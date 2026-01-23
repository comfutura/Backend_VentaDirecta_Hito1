package com.backend.comfutura.controller;

import com.backend.comfutura.dto.request.ImportResultDTO;
import com.backend.comfutura.service.ExcelExportService;
import com.backend.comfutura.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    // ==================== EXPORTACIÓN ====================
    @PostMapping("/export/ots")
    public ResponseEntity<byte[]> exportOts(@RequestBody List<Integer> otIds) {
        try {
            byte[] excelBytes = excelExportService.exportOtsToExcel(otIds);
            return createExcelResponse(excelBytes, "ots_export_" + getTimestamp() + ".xlsx");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exportAllOts() {
        try {
            byte[] excelBytes = excelExportService.exportAllOtsToExcel();
            return createExcelResponse(excelBytes, "todas_ots_" + getTimestamp() + ".xlsx");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== IMPORTACIÓN ====================
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = excelImportService.generateImportTemplate();
            return createExcelResponse(template,
                    "plantilla_importacion_ots_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import/ots")
    public ResponseEntity<ImportResultDTO> importOts(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ImportResultDTO("El archivo está vacío"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body(
                        new ImportResultDTO("Solo se permiten archivos .xlsx"));
            }

            ImportResultDTO result = excelImportService.importOtsFromExcel(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            ImportResultDTO error = new ImportResultDTO("Error en importación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/import/masivo")
    public ResponseEntity<ImportResultDTO> importMasivo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ImportResultDTO("El archivo está vacío"));
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body(
                        new ImportResultDTO("Solo se permiten archivos .xlsx"));
            }

            // Para importación masiva, puedes usar el mismo método o crear uno específico
            ImportResultDTO result = excelImportService.importOtsFromExcel(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            ImportResultDTO error = new ImportResultDTO("Error en importación masiva: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private ResponseEntity<byte[]> createExcelResponse(byte[] excelBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Excel Controller funcionando - " + LocalDateTime.now());
    }
}