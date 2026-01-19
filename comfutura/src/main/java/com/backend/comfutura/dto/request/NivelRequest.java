package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelRequest {
    private String codigo;
    private String nombre;
    private String descripcion;
}
