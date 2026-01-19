package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaRequest {
    private String nombre;
    private Boolean activo;
}