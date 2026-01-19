package com.backend.comfutura.service;
import com.backend.comfutura.dto.response.*;

import java.util.List;

public interface DropdownService {
    List<EmpresaResponse> getEmpresas();
    List<AreaResponse> getAreas();
    List<NivelResponse> getNiveles();
    List<CargoResponse> getCargos();
    List<RolResponse> getRoles();
    List<BancoResponse> getBancos();
    List<UnidadMedidaResponse> getUnidadesMedida();
    List<ClienteResponse> getClientes();
    List<TrabajadorResponse> getTrabajadores();
    List<ProveedorResponse> getProveedores();
    List<MaestroCodigoResponse> getMaestroCodigo();
}
