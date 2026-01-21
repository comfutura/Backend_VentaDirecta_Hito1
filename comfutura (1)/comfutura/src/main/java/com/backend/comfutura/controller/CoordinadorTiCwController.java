package com.backend.comfutura.controller;


import com.backend.comfutura.dto.request.CoordinadorTiCwRequest;
import com.backend.comfutura.dto.response.CoordinadorTiCwResponse;
import com.backend.comfutura.service.CoordinadorTiCwService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordinador-ti-cw")
@RequiredArgsConstructor
public class CoordinadorTiCwController {

    private final CoordinadorTiCwService service;

    // ==============================
    // Listar activos
    // ==============================
    @GetMapping
    public List<CoordinadorTiCwResponse> listar() {
        return service.listarActivos();
    }

    // ==============================
    // Obtener por ID
    // ==============================
    @GetMapping("/{id}")
    public CoordinadorTiCwResponse obtenerPorId(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    // ==============================
    // Crear / Actualizar (MISMO ENDPOINT)
    // ==============================
    @PostMapping
    public CoordinadorTiCwResponse guardar(@RequestBody CoordinadorTiCwRequest request) {
        return service.guardar(request);
    }
}
