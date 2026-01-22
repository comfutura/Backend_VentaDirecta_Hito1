package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.dto.response.OtDetailResponse;
import com.backend.comfutura.dto.response.OtFullResponse;
import com.backend.comfutura.dto.response.OtResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OtService {

    Page<OtResponse> listarOts(Boolean activo, Pageable pageable);

    OtResponse obtenerPorId(Integer idOts);

    OtResponse obtenerPorNumeroOt(Integer numeroOt);

    OtDetailResponse saveOt(OtCreateRequest request);

    void toggleActivo(Integer idOts);

    OtFullResponse obtenerParaEdicion(Integer idOts);

    // Opcional: si en algún momento necesitas el detalle completo por ID
    OtDetailResponse obtenerDetallePorId(Integer idOts);
}