package com.backend.comfutura.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsResponse {
    private Integer idOts;
    private Long ot;
    private String ceco;
    private Integer idCliente;
    private Integer idArea;
    private String descripcion;
    private LocalDateTime fechaApertura;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}