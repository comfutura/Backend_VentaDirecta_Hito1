package com.backend.comfutura.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private Integer idUsuario;
    private String username;
    private Integer idTrabajador;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}