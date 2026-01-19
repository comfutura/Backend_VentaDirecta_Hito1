package com.backend.comfutura.service.serviceImpl;

import com.backend.comfutura.dto.request.UsuarioRequest;
import com.backend.comfutura.dto.response.UsuarioResponse;
import com.backend.comfutura.service.UsuarioService;
import com.backend.comfutura.dto.*;
import com.backend.comfutura.model.*;
import com.backend.comfutura.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    @Override
    public List<UsuarioResponse> getAllUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(u -> UsuarioResponse.builder()
                        .idUsuario(u.getIdUsuario())
                        .username(u.getUsername())
                        .idTrabajador(u.getTrabajador().getIdTrabajador())
                        .activo(u.getActivo())
                        .fechaCreacion(u.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponse createUsuario(UsuarioRequest request) {
        Trabajador trabajador = trabajadorRepository.findById(request.getIdTrabajador())
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // TODO: encriptar
                .trabajador(trabajador)
                .activo(request.getActivo())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .idUsuario(saved.getIdUsuario())
                .username(saved.getUsername())
                .idTrabajador(saved.getTrabajador().getIdTrabajador())
                .activo(saved.getActivo())
                .fechaCreacion(saved.getFechaCreacion())
                .build();
    }

    @Override
    public void asignarRoles(Integer idUsuario, List<Integer> idsRoles) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminar roles antiguos
        List<UsuarioRol> rolesExistentes = usuarioRolRepository.findAll()
                .stream()
                .filter(ur -> ur.getUsuario().getIdUsuario().equals(idUsuario))
                .collect(Collectors.toList());
        usuarioRolRepository.deleteAll(rolesExistentes);

        // Asignar nuevos roles
        for (Integer rolId : idsRoles) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            UsuarioRolId urId = new UsuarioRolId(idUsuario, rolId);
            UsuarioRol ur = UsuarioRol.builder()
                    .id(urId)
                    .usuario(usuario)
                    .rol(rol)
                    .build();
            usuarioRolRepository.save(ur);
        }
    }


    @Override
    public void activarDesactivarUsuario(Integer idUsuario, boolean activo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }
}
