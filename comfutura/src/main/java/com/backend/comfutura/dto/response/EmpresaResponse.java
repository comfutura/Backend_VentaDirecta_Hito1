package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {
    private Integer idEmpresa;
    private String nombre;
    private Boolean activo;
}