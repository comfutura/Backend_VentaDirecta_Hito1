package com.backend.comfutura.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRolResponse {
    private Integer idUsuario;
    private Integer idRol;
}