package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.ExcelImportDTO;
import com.backend.comfutura.dto.request.ImportResultDTO;
import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.record.DropdownDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final OtService otService;
    private final DropdownService dropdownService;

    @Transactional
    public ImportResultDTO importOtsFromExcel(MultipartFile file) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        result.setInicio(System.currentTimeMillis());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Validar que el Excel tenga encabezados correctos
            if (sheet.getLastRowNum() < 0) {
                throw new IOException("El archivo Excel está vacío");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = mapColumnHeaders(headerRow);

            // Verificar encabezados mínimos requeridos
            verificarEncabezadosRequeridos(columnIndex);

            // Procesar filas
            List<ExcelImportDTO> registros = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ExcelImportDTO registro = parseRowToDTO(row, columnIndex, i + 1);
                validarRegistro(registro);
                registros.add(registro);
            }

            result.setTotalRegistros(registros.size());

            // Procesar registros válidos
            List<ExcelImportDTO> exitosos = new ArrayList<>();
            List<ExcelImportDTO> errores = new ArrayList<>();

            for (ExcelImportDTO registro : registros) {
                if (registro.isValido()) {
                    try {
                        OtCreateRequest request = convertirARequest(registro);
                        otService.saveOt(request);
                        registro.setMensajeError("CREADA EXITOSAMENTE");
                        exitosos.add(registro);
                        result.incrementarExitosos();
                    } catch (Exception e) {
                        registro.setValido(false);
                        registro.setMensajeError("Error al guardar: " + e.getMessage());
                        errores.add(registro);
                        result.incrementarFallidos();
                    }
                } else {
                    errores.add(registro);
                    result.incrementarFallidos();
                }
            }

            result.setRegistrosProcesados(exitosos);
            result.setRegistrosConError(errores);
            result.setExito(errores.isEmpty());
            result.setMensaje(result.getExitosos() + " OTs importadas exitosamente");

        } catch (Exception e) {
            throw new IOException("Error al procesar archivo Excel: " + e.getMessage(), e);
        } finally {
            result.setFin(System.currentTimeMillis());
            result.setDuracionMs(result.getFin() - result.getInicio());
        }

        return result;
    }

    @Transactional
    public ImportResultDTO importOtsMasivo(MultipartFile file) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        result.setInicio(System.currentTimeMillis());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = mapColumnHeaders(headerRow);
            verificarEncabezadosRequeridos(columnIndex);

            List<ExcelImportDTO> registros = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ExcelImportDTO registro = parseRowToDTO(row, columnIndex, i + 1);
                validarRegistro(registro);
                registros.add(registro);
            }

            result.setTotalRegistros(registros.size());

            // Procesar en lotes para mejor performance
            int batchSize = 50;
            List<OtCreateRequest> batch = new ArrayList<>();

            for (ExcelImportDTO registro : registros) {
                if (registro.isValido()) {
                    OtCreateRequest request = convertirARequest(registro);
                    batch.add(request);

                    if (batch.size() >= batchSize) {
                        otService.saveOtsMasivo(batch);
                        result.incrementarExitosos(batch.size());
                        batch.clear();
                    }
                } else {
                    result.incrementarFallidos();
                    result.agregarError(registro.getFilaExcel(), registro.getMensajeError());
                }
            }

            // Procesar último lote
            if (!batch.isEmpty()) {
                otService.saveOtsMasivo(batch);
                result.incrementarExitosos(batch.size());
            }

            result.setExito(true);
            result.setMensaje("Importación masiva completada: " + result.getExitosos() + " exitosas, " + result.getFallidos() + " fallidas");

        } catch (Exception e) {
            result.setExito(false);
            result.setMensaje("Error en importación masiva: " + e.getMessage());
            throw new IOException("Error al procesar archivo Excel: " + e.getMessage(), e);
        } finally {
            result.setFin(System.currentTimeMillis());
            result.setDuracionMs(result.getFin() - result.getInicio());
        }

        return result;
    }

    private Map<String, Integer> mapColumnHeaders(Row headerRow) {
        Map<String, Integer> columnIndex = new HashMap<>();

        for (Cell cell : headerRow) {
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String headerName = cell.getStringCellValue().trim().toLowerCase();
                // Normalizar nombres comunes
                headerName = headerName
                        .replace("á", "a").replace("é", "e").replace("í", "i")
                        .replace("ó", "o").replace("ú", "u")
                        .replace("ñ", "n")
                        .replaceAll("[^a-z0-9]", "");
                columnIndex.put(headerName, cell.getColumnIndex());
            }
        }

        return columnIndex;
    }

    private void verificarEncabezadosRequeridos(Map<String, Integer> columnIndex) throws IOException {
        List<String> encabezadosRequeridos = Arrays.asList(
                "descripcion", "fechaapertura", "cliente", "area",
                "proyecto", "fase", "site", "region", "diasasignados", "estado"
        );

        List<String> faltantes = new ArrayList<>();
        for (String requerido : encabezadosRequeridos) {
            if (!columnIndex.containsKey(requerido)) {
                faltantes.add(requerido);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new IOException("Faltan encabezados requeridos: " + String.join(", ", faltantes));
        }
    }

    private ExcelImportDTO parseRowToDTO(Row row, Map<String, Integer> columnIndex, int fila) {
        ExcelImportDTO dto = new ExcelImportDTO();
        dto.setFilaExcel(fila);

        try {
            // 1. DESCRIPCIÓN (obligatorio)
            if (columnIndex.containsKey("descripcion")) {
                dto.setDescripcion(getStringCellValue(row, columnIndex.get("descripcion")));
            }

            // 2. FECHA APERTURA (obligatorio) - CON MEJOR MANEJO DE FECHAS
            if (columnIndex.containsKey("fechaapertura")) {
                LocalDate fecha = parseFecha(row.getCell(columnIndex.get("fechaapertura")));
                dto.setFechaApertura(fecha);
            }

            // 3. CLIENTE (obligatorio)
            if (columnIndex.containsKey("cliente")) {
                dto.setCliente(getStringCellValue(row, columnIndex.get("cliente")));
            }

            // 4. ÁREA (obligatorio)
            if (columnIndex.containsKey("area")) {
                dto.setArea(getStringCellValue(row, columnIndex.get("area")));
            }

            // 5. PROYECTO (obligatorio)
            if (columnIndex.containsKey("proyecto")) {
                dto.setProyecto(getStringCellValue(row, columnIndex.get("proyecto")));
            }

            // 6. FASE (obligatorio)
            if (columnIndex.containsKey("fase")) {
                dto.setFase(getStringCellValue(row, columnIndex.get("fase")));
            }

            // 7. SITE (obligatorio)
            if (columnIndex.containsKey("site")) {
                dto.setSite(getStringCellValue(row, columnIndex.get("site")));
            }

            // 8. REGIÓN (obligatorio)
            if (columnIndex.containsKey("region")) {
                dto.setRegion(getStringCellValue(row, columnIndex.get("region")));
            }

            // 9. DÍAS ASIGNADOS (obligatorio)
            if (columnIndex.containsKey("diasasignados")) {
                dto.setDiasAsignados(getNumericCellValue(row, columnIndex.get("diasasignados")));
            }

            // 10. ESTADO (obligatorio)
            if (columnIndex.containsKey("estado")) {
                dto.setEstado(getStringCellValue(row, columnIndex.get("estado")));
            }

            // 11. OT ANTERIOR (opcional)
            if (columnIndex.containsKey("otanterior") || columnIndex.containsKey("ot anterior")) {
                String key = columnIndex.containsKey("otanterior") ? "otanterior" : "ot anterior";
                Integer otAnterior = getNumericCellValue(row, columnIndex.get(key));
                dto.setOtAnterior(otAnterior);
            }

            // 12. JEFATURA CLIENTE (opcional)
            if (columnIndex.containsKey("jefaturacliente") || columnIndex.containsKey("jefatura")) {
                String key = columnIndex.containsKey("jefaturacliente") ? "jefaturacliente" : "jefatura";
                dto.setJefaturaClienteSolicitante(getStringCellValue(row, columnIndex.get(key)));
            }

            // 13. ANALISTA CLIENTE (opcional)
            if (columnIndex.containsKey("analistacliente") || columnIndex.containsKey("analista")) {
                String key = columnIndex.containsKey("analistacliente") ? "analistacliente" : "analista";
                dto.setAnalistaClienteSolicitante(getStringCellValue(row, columnIndex.get(key)));
            }

        } catch (Exception e) {
            dto.setValido(false);
            dto.setMensajeError("Error al leer datos: " + e.getMessage());
        }

        return dto;
    }

    private LocalDate parseFecha(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // Fecha en formato Excel
                        Date javaDate = cell.getDateCellValue();
                        return javaDate.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } else {
                        // Número que podría ser una fecha serial
                        double numericValue = cell.getNumericCellValue();
                        try {
                            Date date = DateUtil.getJavaDate(numericValue);
                            return date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                        } catch (Exception e) {
                            return null;
                        }
                    }

                case STRING:
                    String dateString = cell.getStringCellValue().trim();
                    if (dateString.isEmpty()) {
                        return null;
                    }

                    // Intentar múltiples formatos de fecha
                    DateTimeFormatter[] formatters = {
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                            DateTimeFormatter.ofPattern("dd/MM/yy"),
                            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                            DateTimeFormatter.ISO_LOCAL_DATE
                    };

                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            return LocalDate.parse(dateString, formatter);
                        } catch (DateTimeParseException e) {
                            // Continuar con el siguiente formato
                        }
                    }

                    // Intentar parsear manualmente
                    return parseFechaManual(dateString);

                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseFechaManual(String dateStr) {
        try {
            // Eliminar espacios y caracteres extraños
            dateStr = dateStr.trim().replaceAll("[^0-9-/.]", "");

            // Dividir por delimitadores comunes
            String[] parts = dateStr.split("[/\\-.]");
            if (parts.length == 3) {
                int day, month, year;

                // Intentar determinar formato (dd/mm/yyyy o mm/dd/yyyy)
                if (parts[0].length() <= 2 && parts[1].length() <= 2) {
                    day = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]);
                    year = Integer.parseInt(parts[2]);

                    // Ajustar año de 2 dígitos
                    if (year < 100) {
                        year += 2000;
                    }

                    return LocalDate.of(year, month, day);
                }
            }
        } catch (Exception e) {
            // Si no se puede parsear, retornar null
        }
        return null;
    }

    private void validarRegistro(ExcelImportDTO dto) {
        List<String> errores = new ArrayList<>();

        // 1. Validar descripción
        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            errores.add("Descripción es obligatoria");
        }

        // 2. Validar fecha apertura
        if (dto.getFechaApertura() == null) {
            errores.add("Fecha de apertura es obligatoria");
        } else {
            LocalDate hoy = LocalDate.now();
            if (dto.getFechaApertura().isAfter(hoy)) {
                errores.add("Fecha de apertura no puede ser futura");
            }
            if (dto.getFechaApertura().isBefore(hoy.minusYears(5))) {
                errores.add("Fecha de apertura no puede ser anterior a 5 años");
            }
        }

        // 3. Validar cliente
        if (dto.getCliente() == null || dto.getCliente().trim().isEmpty()) {
            errores.add("Cliente es obligatorio");
        } else if (!existeCliente(dto.getCliente())) {
            errores.add("Cliente no existe: " + dto.getCliente());
        }

        // 4. Validar área
        if (dto.getArea() == null || dto.getArea().trim().isEmpty()) {
            errores.add("Área es obligatoria");
        }

        // 5. Validar proyecto
        if (dto.getProyecto() == null || dto.getProyecto().trim().isEmpty()) {
            errores.add("Proyecto es obligatorio");
        } else if (!existeProyecto(dto.getProyecto())) {
            errores.add("Proyecto no existe: " + dto.getProyecto());
        }

        // 6. Validar fase
        if (dto.getFase() == null || dto.getFase().trim().isEmpty()) {
            errores.add("Fase es obligatoria");
        } else if (!existeFase(dto.getFase())) {
            errores.add("Fase no existe: " + dto.getFase());
        }

        // 7. Validar site
        if (dto.getSite() == null || dto.getSite().trim().isEmpty()) {
            errores.add("Site es obligatorio");
        } else if (!existeSite(dto.getSite())) {
            errores.add("Site no existe: " + dto.getSite());
        }

        // 8. Validar región
        if (dto.getRegion() == null || dto.getRegion().trim().isEmpty()) {
            errores.add("Región es obligatoria");
        } else if (!existeRegion(dto.getRegion())) {
            errores.add("Región no existe: " + dto.getRegion());
        }

        // 9. Validar días asignados
        if (dto.getDiasAsignados() == null) {
            errores.add("Días asignados es obligatorio");
        } else if (dto.getDiasAsignados() <= 0) {
            errores.add("Días asignados debe ser mayor a 0");
        } else if (dto.getDiasAsignados() > 365) {
            errores.add("Días asignados no puede ser mayor a 365");
        }

        // 10. Validar estado
        if (dto.getEstado() == null || dto.getEstado().trim().isEmpty()) {
            errores.add("Estado es obligatorio");
        } else if (!esEstadoValido(dto.getEstado())) {
            errores.add("Estado no válido: " + dto.getEstado() + ". Válidos: ASIGNACION, EN PROCESO, FINALIZADA, CANCELADA");
        }

        if (!errores.isEmpty()) {
            dto.setValido(false);
            dto.setMensajeError(String.join("; ", errores));
        }
    }

    private OtCreateRequest convertirARequest(ExcelImportDTO importDTO) {
        OtCreateRequest request = new OtCreateRequest();

        // Campos obligatorios
        request.setDescripcion(importDTO.getDescripcion());
        request.setFechaApertura(importDTO.getFechaApertura());
        request.setDiasAsignados(importDTO.getDiasAsignados());
        request.setActivo(true);

        // Mapear IDs por nombres
        request.setIdCliente(buscarIdPorNombre(dropdownService.getClientes(), importDTO.getCliente()));
        request.setIdArea(buscarIdPorNombre(dropdownService.getAreas(), importDTO.getArea()));
        request.setIdProyecto(buscarIdPorNombre(dropdownService.getProyectos(), importDTO.getProyecto()));
        request.setIdFase(buscarIdPorNombre(dropdownService.getFases(), importDTO.getFase()));
        request.setIdSite(buscarIdPorNombre(dropdownService.getSites(), importDTO.getSite()));
        request.setIdRegion(buscarIdPorNombre(dropdownService.getRegiones(), importDTO.getRegion()));
        request.setIdEstadoOt(buscarIdPorNombre(dropdownService.getEstadosOt(), importDTO.getEstado()));

        // Campo opcional
        request.setIdOtsAnterior(importDTO.getOtAnterior());

        // Campos de texto
        request.setJefaturaClienteSolicitante(importDTO.getJefaturaClienteSolicitante());
        request.setAnalistaClienteSolicitante(importDTO.getAnalistaClienteSolicitante());

        return request;
    }

    // Métodos auxiliares
    private String getStringCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    private Integer getNumericCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) return null;
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getStringCellValue(row, i);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean existeCliente(String nombre) {
        return dropdownService.getClientes().stream()
                .anyMatch(c -> c.label().equalsIgnoreCase(nombre.trim()));
    }

    private boolean existeProyecto(String nombre) {
        return dropdownService.getProyectos().stream()
                .anyMatch(p -> p.label().equalsIgnoreCase(nombre.trim()));
    }

    private boolean existeFase(String nombre) {
        return dropdownService.getFases().stream()
                .anyMatch(f -> f.label().equalsIgnoreCase(nombre.trim()));
    }

    private boolean existeSite(String nombre) {
        return dropdownService.getSites().stream()
                .anyMatch(s -> s.label().equalsIgnoreCase(nombre.trim()));
    }

    private boolean existeRegion(String nombre) {
        return dropdownService.getRegiones().stream()
                .anyMatch(r -> r.label().equalsIgnoreCase(nombre.trim()));
    }

    private boolean esEstadoValido(String estado) {
        List<String> estadosValidos = Arrays.asList("ASIGNACION", "EN PROCESO", "FINALIZADA", "CANCELADA");
        return estadosValidos.contains(estado.toUpperCase());
    }

    private Integer buscarIdPorNombre(List<DropdownDTO> lista, String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        String searchName = nombre.trim();
        return lista.stream()
                .filter(dto -> dto.label() != null && dto.label().trim().equalsIgnoreCase(searchName))
                .map(DropdownDTO::id)
                .findFirst()
                .orElse(null);
    }

    // Método para generar plantilla mejorada
    public byte[] generateImportTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Hoja principal de plantilla
            Sheet plantillaSheet = workbook.createSheet("Plantilla Importación");

            // Crear encabezados con colores
            Row headerRow = plantillaSheet.createRow(0);
            String[] headers = {
                    "Descripción *",
                    "Fecha Apertura * (dd/mm/aaaa)",
                    "Cliente *",
                    "Área *",
                    "Proyecto *",
                    "Fase *",
                    "Site *",
                    "Región *",
                    "Días Asignados *",
                    "Estado *",
                    "OT Anterior (opcional)",
                    "Jefatura Cliente (opcional)",
                    "Analista Cliente (opcional)"
            };

            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar filas de ejemplo
            agregarFilasEjemplo(plantillaSheet);

            // Crear hojas de referencia
            crearHojasReferencia(workbook);

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                plantillaSheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando plantilla", e);
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void agregarFilasEjemplo(Sheet sheet) {
        Row ejemplo1 = sheet.createRow(1);
        ejemplo1.createCell(0).setCellValue("Instalación de equipos en sitio Lima Norte");
        ejemplo1.createCell(1).setCellValue("15/01/2026");
        ejemplo1.createCell(2).setCellValue("Cliente Ejemplo S.A.");
        ejemplo1.createCell(3).setCellValue("TI");
        ejemplo1.createCell(4).setCellValue("Proyecto Digitalización");
        ejemplo1.createCell(5).setCellValue("Implementación");
        ejemplo1.createCell(6).setCellValue("Lima01");
        ejemplo1.createCell(7).setCellValue("Lima");
        ejemplo1.createCell(8).setCellValue(15);
        ejemplo1.createCell(9).setCellValue("ASIGNACION");
        ejemplo1.createCell(10).setCellValue("20240099");
        ejemplo1.createCell(11).setCellValue("Juan Pérez");
        ejemplo1.createCell(12).setCellValue("María Gómez");
    }

    private void crearHojasReferencia(Workbook workbook) {
        // Hoja de clientes
        Sheet clientesSheet = workbook.createSheet("Clientes");
        Row headerClientes = clientesSheet.createRow(0);
        headerClientes.createCell(0).setCellValue("CLIENTES DISPONIBLES:");

        List<DropdownDTO> clientes = dropdownService.getClientes();
        for (int i = 0; i < clientes.size(); i++) {
            Row row = clientesSheet.createRow(i + 1);
            row.createCell(0).setCellValue(clientes.get(i).label());
        }
        clientesSheet.autoSizeColumn(0);

        // Hoja de proyectos
        Sheet proyectosSheet = workbook.createSheet("Proyectos");
        Row headerProyectos = proyectosSheet.createRow(0);
        headerProyectos.createCell(0).setCellValue("PROYECTOS DISPONIBLES:");

        List<DropdownDTO> proyectos = dropdownService.getProyectos();
        for (int i = 0; i < proyectos.size(); i++) {
            Row row = proyectosSheet.createRow(i + 1);
            row.createCell(0).setCellValue(proyectos.get(i).label());
        }
        proyectosSheet.autoSizeColumn(0);

        // Hoja de estados
        Sheet estadosSheet = workbook.createSheet("Estados");
        Row headerEstados = estadosSheet.createRow(0);
        headerEstados.createCell(0).setCellValue("ESTADOS VÁLIDOS:");

        String[][] estados = {
                {"ASIGNACION", "Estado inicial de asignación"},
                {"EN PROCESO", "OT en ejecución"},
                {"FINALIZADA", "OT completada exitosamente"},
                {"CANCELADA", "OT cancelada"}
        };

        for (int i = 0; i < estados.length; i++) {
            Row row = estadosSheet.createRow(i + 1);
            row.createCell(0).setCellValue(estados[i][0]);
            row.createCell(1).setCellValue(estados[i][1]);
        }
        estadosSheet.autoSizeColumn(0);
        estadosSheet.autoSizeColumn(1);
    }

    // Métodos públicos para obtener listas (para el controlador)
    public List<String[]> getAvailableClients() {
        return dropdownService.getClientes().stream()
                .map(dto -> new String[]{dto.label(), "ID: " + dto.id()})
                .collect(Collectors.toList());
    }

    public List<String[]> getAvailableProjects() {
        return dropdownService.getProyectos().stream()
                .map(dto -> new String[]{dto.label(), "ID: " + dto.id()})
                .collect(Collectors.toList());
    }

    public List<String[]> getAvailablePhases() {
        return dropdownService.getFases().stream()
                .map(dto -> new String[]{dto.label(), "ID: " + dto.id()})
                .collect(Collectors.toList());
    }

    public List<String[]> getAvailableSites() {
        return dropdownService.getSites().stream()
                .map(dto -> new String[]{dto.label(), "ID: " + dto.id()})
                .collect(Collectors.toList());
    }

    public List<String[]> getAvailableRegions() {
        return dropdownService.getRegiones().stream()
                .map(dto -> new String[]{dto.label(), "ID: " + dto.id()})
                .collect(Collectors.toList());
    }
}