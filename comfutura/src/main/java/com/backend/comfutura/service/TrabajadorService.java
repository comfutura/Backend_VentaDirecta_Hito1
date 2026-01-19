package com.backend.comfutura.service;

import com.backend.comfutura.dto.request.TrabajadorRequest;
import com.backend.comfutura.dto.response.ClienteResponse;
import com.backend.comfutura.dto.response.TrabajadorResponse;

import java.util.List;

public interface TrabajadorService {
    List<TrabajadorResponse> getAllTrabajadores();
    TrabajadorResponse getTrabajadorById(Integer id);
    TrabajadorResponse createTrabajador(TrabajadorRequest request);
    TrabajadorResponse updateTrabajador(Integer id, TrabajadorRequest request);
    void activarDesactivarTrabajador(Integer id, boolean activo);

    List<ClienteResponse> getClientesByTrabajador(Integer id);
    void asignarCliente(Integer idTrabajador, Integer idCliente);
    void eliminarCliente(Integer idTrabajador, Integer idCliente);
}
