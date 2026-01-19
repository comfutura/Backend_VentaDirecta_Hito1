package com.backend.comfutura.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaestroCodigoResponse {
    private Integer idMaestro;
    private String codigo;
    private String descripcion;
    private Integer idUnidadMedida;
    private Double precioBase;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}