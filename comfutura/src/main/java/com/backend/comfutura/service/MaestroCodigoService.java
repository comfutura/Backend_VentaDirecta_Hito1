package com.backend.comfutura.service;
import com.backend.comfutura.dto.request.MaestroCodigoRequest;
import com.backend.comfutura.dto.response.MaestroCodigoResponse;

import java.util.List;

public interface MaestroCodigoService {
    List<MaestroCodigoResponse> getAllMaestroCodigo();
    MaestroCodigoResponse createMaestroCodigo(MaestroCodigoRequest request);
    MaestroCodigoResponse updateMaestroCodigo(Integer id, MaestroCodigoRequest request);
    void activarDesactivarMaestroCodigo(Integer id, boolean activo);
}
