// ExcelBatchService.java
package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.ExcelImportDTO;
import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.record.DropdownDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExcelBatchService {

    private final OtService otService;
    private final ExcelImportService excelImportService;
    private final DropdownService dropdownService;

    @Async
    @Transactional
    public CompletableFuture<BatchResult> processBatchImport(MultipartFile file, String usuario) {
        BatchResult result = new BatchResult();
        result.setUsuario(usuario);
        result.setInicio(System.currentTimeMillis());

        try {
            // Procesar en lotes de 50 registros
            List<ExcelImportDTO> allRecords = parseExcelFile(file);
            result.setTotalRegistros(allRecords.size());

            // Dividir en lotes
            int batchSize = 50;
            int totalBatches = (int) Math.ceil((double) allRecords.size() / batchSize);

            for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
                int fromIndex = batchNum * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, allRecords.size());
                List<ExcelImportDTO> batch = allRecords.subList(fromIndex, toIndex);

                processBatch(batch, result);

                // Actualizar progreso
                result.setProgreso((batchNum + 1) * 100 / totalBatches);
            }

            result.setExito(true);
            result.setMensaje("Importación completada exitosamente");

        } catch (Exception e) {
            result.setExito(false);
            result.setMensaje("Error en importación: " + e.getMessage());
            e.printStackTrace();
        }

        result.setFin(System.currentTimeMillis());
        result.setDuracionMs(result.getFin() - result.getInicio());

        return CompletableFuture.completedFuture(result);
    }

    private void processBatch(List<ExcelImportDTO> batch, BatchResult result) {
        for (ExcelImportDTO importDTO : batch) {
            try {
                if (importDTO.isValido()) {
                    OtCreateRequest request = convertToRequest(importDTO);
                    otService.saveOt(request);
                    result.incrementExitosos();
                } else {
                    result.incrementFallidos();
                    result.agregarError(importDTO.getFilaExcel(), importDTO.getMensajeError());
                }
            } catch (Exception e) {
                result.incrementFallidos();
                result.agregarError(importDTO.getFilaExcel(), "Error al guardar: " + e.getMessage());
            }
        }
    }

    // MÉTODO parseExcelFile QUE TE FALTABA
    private List<ExcelImportDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<ExcelImportDTO> records = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Primera hoja

            // Validar que tiene encabezados
            if (sheet.getLastRowNum() < 0) {
                throw new IOException("El archivo Excel está vacío");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IOException("No se encontraron encabezados en el Excel");
            }

            // Mapear índices de columnas
            Map<String, Integer> columnIndex = mapColumnHeaders(headerRow);
            System.out.println("Columnas encontradas: " + columnIndex.keySet());

            // Procesar filas de datos
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                ExcelImportDTO importDTO = parseRowToDTO(row, columnIndex, i + 1);
                validateOtImport(importDTO);
                records.add(importDTO);
            }

            System.out.println("Total registros leídos: " + records.size());

        } catch (Exception e) {
            throw new IOException("Error al parsear archivo Excel: " + e.getMessage(), e);
        }

        return records;
    }

    // MÉTODO convertToRequest QUE TE FALTABA
    private OtCreateRequest convertToRequest(ExcelImportDTO importDTO) {
        OtCreateRequest request = new OtCreateRequest();

        // Mapear campos básicos
        request.setDescripcion(importDTO.getDescripcion());
        request.setFechaApertura(importDTO.getFechaApertura());
        request.setIdOtsAnterior(importDTO.getOtAnterior());
        request.setIdOts(importDTO.getOt());

        // Buscar IDs por nombres usando dropdownService
        if (importDTO.getCliente() != null) {
            Integer clienteId = findIdByName(dropdownService.getClientes(), importDTO.getCliente());
            if (clienteId != null) {
                request.setIdCliente(clienteId);

                // Si tenemos cliente, buscar área asociada
                if (importDTO.getArea() != null) {
                    List<DropdownDTO> areas = dropdownService.getAreasByCliente(clienteId);
                    Integer areaId = findIdByName(areas, importDTO.getArea());
                    if (areaId != null) request.setIdArea(areaId);
                }
            }
        }

        // Proyecto
        if (importDTO.getProyecto() != null) {
            Integer proyectoId = findIdByName(dropdownService.getProyectos(), importDTO.getProyecto());
            if (proyectoId != null) request.setIdProyecto(proyectoId);
        }

        // Fase
        if (importDTO.getFase() != null) {
            Integer faseId = findIdByName(dropdownService.getFases(), importDTO.getFase());
            if (faseId != null) request.setIdFase(faseId);
        }

        // Site
        if (importDTO.getSite() != null) {
            Integer siteId = findIdByName(dropdownService.getSites(), importDTO.getSite());
            if (siteId != null) request.setIdSite(siteId);
        }

        // Región
        if (importDTO.getRegion() != null) {
            Integer regionId = findIdByName(dropdownService.getRegiones(), importDTO.getRegion());
            if (regionId != null) request.setIdRegion(regionId);
        }

        // Por defecto
        request.setActivo(true);

        return request;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, Integer> mapColumnHeaders(Row headerRow) {
        Map<String, Integer> columnIndex = new HashMap<>();

        for (Cell cell : headerRow) {
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String headerName = cell.getStringCellValue().trim().toLowerCase()
                        .replace("á", "a").replace("é", "e").replace("í", "i")
                        .replace("ó", "o").replace("ú", "u"); // Normalizar acentos
                columnIndex.put(headerName, cell.getColumnIndex());
            }
        }

        return columnIndex;
    }

    private ExcelImportDTO parseRowToDTO(Row row, Map<String, Integer> columnIndex, int fila) {
        ExcelImportDTO dto = new ExcelImportDTO();
        dto.setFilaExcel(fila);

        try {
            // OT (opcional)
            if (columnIndex.containsKey("ot")) {
                Cell cell = row.getCell(columnIndex.get("ot"));
                if (cell != null && !isCellEmpty(cell)) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        dto.setOt((int) cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        try {
                            dto.setOt(Integer.parseInt(cell.getStringCellValue().trim()));
                        } catch (NumberFormatException e) {
                            // Si no es número, dejarlo null
                        }
                    }
                }
            }

            // Descripción (requerida)
            if (columnIndex.containsKey("descripción") || columnIndex.containsKey("descripcion")) {
                String key = columnIndex.containsKey("descripción") ? "descripción" : "descripcion";
                Cell cell = row.getCell(columnIndex.get(key));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setDescripcion(getCellValueAsString(cell));
                }
            }

            // Fecha Apertura (requerida)
            if (columnIndex.containsKey("fecha apertura") || columnIndex.containsKey("fecha")) {
                String key = columnIndex.containsKey("fecha apertura") ? "fecha apertura" : "fecha";
                Cell cell = row.getCell(columnIndex.get(key));
                if (cell != null && !isCellEmpty(cell)) {
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        dto.setFechaApertura(new java.sql.Date(date.getTime()).toLocalDate());
                    } else if (cell.getCellType() == CellType.STRING) {
                        // Intentar parsear fecha en formato texto
                        String dateStr = cell.getStringCellValue().trim();
                        LocalDate date = parseDateString(dateStr);
                        if (date != null) {
                            dto.setFechaApertura(date);
                        }
                    }
                }
            }

            // Cliente (requerido)
            if (columnIndex.containsKey("cliente")) {
                Cell cell = row.getCell(columnIndex.get("cliente"));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setCliente(getCellValueAsString(cell));
                }
            }

            // Área
            if (columnIndex.containsKey("área") || columnIndex.containsKey("area")) {
                String key = columnIndex.containsKey("área") ? "área" : "area";
                Cell cell = row.getCell(columnIndex.get(key));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setArea(getCellValueAsString(cell));
                }
            }

            // Proyecto
            if (columnIndex.containsKey("proyecto")) {
                Cell cell = row.getCell(columnIndex.get("proyecto"));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setProyecto(getCellValueAsString(cell));
                }
            }

            // Fase
            if (columnIndex.containsKey("fase")) {
                Cell cell = row.getCell(columnIndex.get("fase"));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setFase(getCellValueAsString(cell));
                }
            }

            // Site
            if (columnIndex.containsKey("site")) {
                Cell cell = row.getCell(columnIndex.get("site"));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setSite(getCellValueAsString(cell));
                }
            }

            // Región
            if (columnIndex.containsKey("región") || columnIndex.containsKey("region")) {
                String key = columnIndex.containsKey("región") ? "región" : "region";
                Cell cell = row.getCell(columnIndex.get(key));
                if (cell != null && !isCellEmpty(cell)) {
                    dto.setRegion(getCellValueAsString(cell));
                }
            }

        } catch (Exception e) {
            dto.setValido(false);
            dto.setMensajeError("Error al leer datos: " + e.getMessage());
        }

        return dto;
    }

    private void validateOtImport(ExcelImportDTO dto) {
        List<String> errores = new ArrayList<>();

        // Validaciones básicas
        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            errores.add("Descripción es requerida");
        }

        if (dto.getFechaApertura() == null) {
            errores.add("Fecha de apertura es requerida");
        } else if (dto.getFechaApertura().isAfter(LocalDate.now())) {
            errores.add("Fecha de apertura no puede ser futura");
        }

        // Validar que el cliente existe
        if (dto.getCliente() != null && !clienteExiste(dto.getCliente())) {
            errores.add("Cliente no encontrado: " + dto.getCliente());
        }

        if (!errores.isEmpty()) {
            dto.setValido(false);
            dto.setMensajeError(String.join("; ", errores));
        }
    }

    private boolean clienteExiste(String nombreCliente) {
        return dropdownService.getClientes().stream()
                .anyMatch(c -> c.label().equalsIgnoreCase(nombreCliente.trim()));
    }

    private Integer findIdByName(List<DropdownDTO> dropdown, String name) {
        if (dropdown == null || name == null || name.trim().isEmpty()) {
            return null;
        }

        String searchName = name.trim();
        return dropdown.stream()
                .filter(d -> d.label() != null && d.label().trim().equalsIgnoreCase(searchName))
                .map(DropdownDTO::id)
                .findFirst()
                .orElse(null);
    }

    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCellEmpty(Cell cell) {
        if (cell == null) return true;
        if (cell.getCellType() == CellType.BLANK) return true;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim().isEmpty();
        }
        return false;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
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

    private LocalDate parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // Remover espacios y caracteres extraños
        dateStr = dateStr.trim().replaceAll("\\s+", " ");

        // Intentar diferentes formatos de fecha
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e) {
                // Continuar con el siguiente formato
            }
        }

        // Si no se pudo parsear con formatters estándar, intentar parsear manualmente
        return parseDateManually(dateStr);
    }

    private LocalDate parseDateManually(String dateStr) {
        try {
            // Intentar detectar formato común "dd/mm/yyyy"
            String[] parts = dateStr.split("[/\\-]");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                int year = Integer.parseInt(parts[2].trim());

                // Ajustar año de 2 dígitos a 4 dígitos
                if (year < 100) {
                    year = year + 2000; // Asume años 2000+
                }

                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            // Si falla, devolver null
        }

        return null;
    }
    // Clase interna para resultado del batch
    @Data
    public static class BatchResult {
        private String usuario;
        private long inicio;
        private long fin;
        private long duracionMs;
        private int totalRegistros;
        private int exitosos = 0;
        private int fallidos = 0;
        private int progreso = 0;
        private boolean exito = false;
        private String mensaje;
        private Map<Integer, String> errores = new HashMap<>();

        public void incrementExitosos() {
            this.exitosos++;
        }

        public void incrementFallidos() {
            this.fallidos++;
        }

        public void agregarError(int fila, String error) {
            this.errores.put(fila, error);
        }
    }
}