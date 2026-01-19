package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsDetalleResponse {
    private Integer idOtsDetalle;
    private Integer idOts;
    private Integer idMaestro;
    private Integer idProveedor;
    private Double cantidad;
    private Double precioUnitario;
}
