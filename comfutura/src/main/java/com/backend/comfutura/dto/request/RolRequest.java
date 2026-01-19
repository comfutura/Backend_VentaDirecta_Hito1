package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequest {
    private String nombre;
    private String descripcion;
}
