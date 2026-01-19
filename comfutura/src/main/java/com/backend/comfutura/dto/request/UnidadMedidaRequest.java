package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedidaRequest {
    private String codigo;
    private String descripcion;
    private Boolean activo;
}