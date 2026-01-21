package com.backend.comfutura.service.serviceImpl;



import com.backend.comfutura.dto.request.CoordinadorTiCwRequest;
import com.backend.comfutura.dto.response.CoordinadorTiCwResponse;
import com.backend.comfutura.model.CoordinadorTiCwPextEnergia;
import com.backend.comfutura.repository.CoordinadorTiCwRepository;
import com.backend.comfutura.service.CoordinadorTiCwService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinadorTiCwServiceImpl implements CoordinadorTiCwService {

    private final CoordinadorTiCwRepository repository;

    // ==============================
    // Listar activos
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorTiCwResponse> listarActivos() {
        return repository.findByActivoTrueOrderByDescripcionAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==============================
    // Crear / Actualizar (JUNTOS)
    // ==============================
    @Override
    @Transactional
    public CoordinadorTiCwResponse guardar(CoordinadorTiCwRequest request) {

        CoordinadorTiCwPextEnergia entity;

        if (request.getId() != null) {
            // UPDATE
            entity = repository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Coordinador no encontrado"));
        } else {
            // CREATE
            entity = new CoordinadorTiCwPextEnergia();
            entity.setActivo(true);
        }

        entity.setDescripcion(request.getDescripcion());

        if (request.getActivo() != null) {
            entity.setActivo(request.getActivo());
        }

        return mapToResponse(repository.save(entity));
    }

    // ==============================
    // Obtener por ID
    // ==============================
    @Override
    @Transactional(readOnly = true)
    public CoordinadorTiCwResponse obtenerPorId(Integer id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Coordinador no encontrado"));
    }

    // ==============================
    // Mapper
    // ==============================
    private CoordinadorTiCwResponse mapToResponse(CoordinadorTiCwPextEnergia entity) {
        return CoordinadorTiCwResponse.builder()
                .id(entity.getId())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .build();
    }
}

