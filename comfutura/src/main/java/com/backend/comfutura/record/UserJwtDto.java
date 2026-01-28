package com.backend.comfutura.record;

import java.util.List;

public record UserJwtDto(
        Integer idUsuario,
        Integer idTrabajador,
        String username,
        String nombreCompleto,
        Boolean activo,
        List<String> nivel
) {}
