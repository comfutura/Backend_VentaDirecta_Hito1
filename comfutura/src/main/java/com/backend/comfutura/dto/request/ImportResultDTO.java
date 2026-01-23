package com.backend.comfutura.dto.request;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ImportResultDTO {
    private int totalRegistros;
    private int exitosos = 0;
    private int fallidos = 0;
    private long inicio;
    private long fin;
    private long duracionMs;
    private boolean exito = false;
    private String mensaje;
    private List<ExcelImportDTO> registrosProcesados = new ArrayList<>();
    private List<ExcelImportDTO> registrosConError = new ArrayList<>();
    private Map<Integer, String> erroresDetallados = new HashMap<>();

    public ImportResultDTO() {
        this.inicio = System.currentTimeMillis();
    }

    public ImportResultDTO(String mensaje) {
        this();
        this.mensaje = mensaje;
    }

    public void incrementarExitosos() {
        this.exitosos++;
    }

    public void incrementarExitosos(int cantidad) {
        this.exitosos += cantidad;
    }

    public void incrementarFallidos() {
        this.fallidos++;
    }

    public void agregarError(int fila, String error) {
        this.erroresDetallados.put(fila, error);
    }

    public void finalizar() {
        this.fin = System.currentTimeMillis();
        this.duracionMs = this.fin - this.inicio;
    }
}