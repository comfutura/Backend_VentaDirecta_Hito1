package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedidaResponse {
    private Integer idUnidadMedida;
    private String codigo;
    private String descripcion;
    private Boolean activo;
}
