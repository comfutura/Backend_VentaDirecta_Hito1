package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajadorClienteResponse {
    private Integer idTrabajador;
    private Integer idCliente;
}