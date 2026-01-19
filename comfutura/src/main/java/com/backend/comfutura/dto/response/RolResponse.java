package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolResponse {
    private Integer idRol;
    private String nombre;
    private String descripcion;
}