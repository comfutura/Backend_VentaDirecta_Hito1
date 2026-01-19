package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoRequest {
    private String nombre;
    private Integer idNivel;
    private Boolean activo;
}