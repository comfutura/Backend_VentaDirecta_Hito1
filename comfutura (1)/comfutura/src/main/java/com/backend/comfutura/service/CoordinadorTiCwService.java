package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.CoordinadorTiCwRequest;
import com.backend.comfutura.dto.response.CoordinadorTiCwResponse;

import java.util.List;

public interface CoordinadorTiCwService {

    List<CoordinadorTiCwResponse> listarActivos();

    CoordinadorTiCwResponse guardar(CoordinadorTiCwRequest request);

    CoordinadorTiCwResponse obtenerPorId(Integer id);
}
