package com.backend.comfutura.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinadorTiCwResponse {

    private Integer id;
    private String descripcion;
    private Boolean activo;
}
