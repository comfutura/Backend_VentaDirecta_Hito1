package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.ClienteRequest;
import com.backend.comfutura.dto.response.ClienteResponse;

import java.util.List;

public interface ClienteService {
    List<ClienteResponse> getAllClientes();
    ClienteResponse getClienteById(Integer id);
    ClienteResponse createCliente(ClienteRequest request);
    ClienteResponse updateCliente(Integer id, ClienteRequest request);
    void activarDesactivarCliente(Integer id, boolean activo);
}

