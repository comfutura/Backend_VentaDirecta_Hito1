package com.backend.comfutura.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Para formulario de edición (solo IDs + datos básicos)
@Data
@Builder
public class OtFullResponse {   // o renombrar a OtEditResponse

    private Integer idOts;
    private Integer ot;
    private Integer idOtsAnterior;

    private Integer idCliente;
    private Integer idArea;
    private Integer idProyecto;
    private Integer idFase;
    private Integer idSite;
    private Integer idRegion;

    private String descripcion;
    private LocalDate fechaApertura;

    // Solicitante cliente
    private Integer idJefaturaClienteSolicitante;
    private Integer idAnalistaClienteSolicitante;

    // Responsables CW (IDs)
    private Integer idCreador;                    // ← importante: readonly en frontend
    private Integer idCoordinadorTiCw;
    private Integer idJefaturaResponsable;
    private Integer idLiquidador;
    private Integer idEjecutante;
    private Integer idAnalistaContable;

    private Boolean activo;
    private LocalDateTime fechaCreacion;
}