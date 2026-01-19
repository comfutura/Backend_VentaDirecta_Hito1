package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BancoResponse {
    private Integer idBanco;
    private String nombre;
    private Boolean activo;
}