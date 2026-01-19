package com.backend.comfutura.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsRequest {
    private Long ot;
    private String ceco;
    private Integer idCliente;
    private Integer idArea;
    private String descripcion;
    private LocalDateTime fechaApertura;
    private Boolean activo;
}
