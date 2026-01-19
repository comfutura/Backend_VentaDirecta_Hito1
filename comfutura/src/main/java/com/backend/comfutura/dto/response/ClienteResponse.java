package com.backend.comfutura.dto.response;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {
    private Integer idCliente;
    private String razonSocial;
    private String ruc;
    private Boolean activo;
}