package com.backend.comfutura.service;


import com.backend.comfutura.dto.request.ProveedorRequest;
import com.backend.comfutura.dto.response.ProveedorResponse;

import java.util.List;

public interface ProveedorService {
    List<ProveedorResponse> getAllProveedores();
    ProveedorResponse getProveedorById(Integer id);
    ProveedorResponse createProveedor(ProveedorRequest request);
    ProveedorResponse updateProveedor(Integer id, ProveedorRequest request);
}
