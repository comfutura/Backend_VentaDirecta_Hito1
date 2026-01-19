package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.UsuarioRequest;
import com.backend.comfutura.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> getAllUsuarios();
    UsuarioResponse createUsuario(UsuarioRequest request);
    void asignarRoles(Integer idUsuario, List<Integer> idsRoles);
    void activarDesactivarUsuario(Integer idUsuario, boolean activo);
}
