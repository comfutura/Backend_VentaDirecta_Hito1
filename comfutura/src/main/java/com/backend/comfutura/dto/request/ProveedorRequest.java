package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorRequest {
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;
    private String contacto;
    private String telefono;
    private String correo;
    private Integer idBanco;
    private String numeroCuenta;
    private String moneda;
    private Boolean activo;
}
