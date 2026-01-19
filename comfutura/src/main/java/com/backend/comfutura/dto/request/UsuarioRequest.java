package com.backend.comfutura.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequest {
    private String username;
    private String password;
    private Integer idTrabajador;
    private Boolean activo;
}
