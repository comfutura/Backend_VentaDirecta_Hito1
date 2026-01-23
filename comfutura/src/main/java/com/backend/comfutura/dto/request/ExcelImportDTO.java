package com.backend.comfutura.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExcelImportDTO {
    // Campos obligatorios
    private String descripcion;
    private LocalDate fechaApertura;
    private String cliente;
    private String area;
    private String proyecto;
    private String fase;
    private String site;
    private String region;
    private Integer diasAsignados;
    private String estado;

    // Campos opcionales
    private Integer ot; // OT específica (opcional)
    private Integer otAnterior;

    // Campos de texto (sin ID)
    private String jefaturaClienteSolicitante;
    private String analistaClienteSolicitante;

    // Campos de responsables (opcionales)
    private String coordinadorTiCw;
    private String jefaturaResponsable;
    private String liquidador;
    private String ejecutante;
    private String analistaContable;

    // Para resultado
    private int filaExcel;
    private boolean valido = true;
    private String mensajeError;
}