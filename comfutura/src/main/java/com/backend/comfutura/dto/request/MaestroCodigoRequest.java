package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaestroCodigoRequest {
    private String codigo;
    private String descripcion;
    private Integer idUnidadMedida;
    private Double precioBase;
    private Boolean activo;
}