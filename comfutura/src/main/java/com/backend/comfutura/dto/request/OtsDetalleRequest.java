package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsDetalleRequest {
    private Integer idOts;
    private Integer idMaestro;
    private Integer idProveedor;
    private Double cantidad;
    private Double precioUnitario;
}