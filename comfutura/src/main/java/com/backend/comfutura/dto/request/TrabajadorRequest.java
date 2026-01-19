package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajadorRequest {
    private String nombres;
    private String apellidos;
    private String dni;
    private String celular;
    private String correoCorporativo;
    private Integer idEmpresa;
    private Integer idArea;
    private Integer idCargo;
    private Boolean activo;
}