package com.backend.comfutura.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse {
    private Integer idProveedor;
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
    private LocalDateTime fechaCreacion;
}
