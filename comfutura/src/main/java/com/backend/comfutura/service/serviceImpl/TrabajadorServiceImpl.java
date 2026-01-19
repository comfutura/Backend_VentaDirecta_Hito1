package com.backend.comfutura.service.serviceImpl;

import com.backend.comfutura.dto.*;
import com.backend.comfutura.dto.request.TrabajadorRequest;
import com.backend.comfutura.dto.response.ClienteResponse;
import com.backend.comfutura.dto.response.TrabajadorResponse;
import com.backend.comfutura.model.*;
import com.backend.comfutura.repository.*;
import com.backend.comfutura.service.TrabajadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrabajadorServiceImpl implements TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final EmpresaRepository empresaRepository;
    private final AreaRepository areaRepository;
    private final CargoRepository cargoRepository;
    private final ClienteRepository clienteRepository;
    private final TrabajadorClienteRepository trabajadorClienteRepository;

    @Override
    public List<TrabajadorResponse> getAllTrabajadores() {
        return trabajadorRepository.findAll()
                .stream()
                .map(t -> TrabajadorResponse.builder()
                        .idTrabajador(t.getIdTrabajador())
                        .nombres(t.getNombres())
                        .apellidos(t.getApellidos())
                        .dni(t.getDni())
                        .idEmpresa(t.getEmpresa().getIdEmpresa())
                        .idArea(t.getArea().getIdArea())
                        .idCargo(t.getCargo().getIdCargo())
                        .activo(t.getActivo())
                        .fechaCreacion(t.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public TrabajadorResponse getTrabajadorById(Integer id) {
        Trabajador t = trabajadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

        return TrabajadorResponse.builder()
                .idTrabajador(t.getIdTrabajador())
                .nombres(t.getNombres())
                .apellidos(t.getApellidos())
                .dni(t.getDni())
                .idEmpresa(t.getEmpresa().getIdEmpresa())
                .idArea(t.getArea().getIdArea())
                .idCargo(t.getCargo().getIdCargo())
                .activo(t.getActivo())
                .fechaCreacion(t.getFechaCreacion())
                .build();
    }

    @Override
    public TrabajadorResponse createTrabajador(TrabajadorRequest request) {
        Empresa empresa = empresaRepository.findById(request.getIdEmpresa())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        Area area = areaRepository.findById(request.getIdArea())
                .orElseThrow(() -> new RuntimeException("Área no encontrada"));
        Cargo cargo = cargoRepository.findById(request.getIdCargo())
                .orElseThrow(() -> new RuntimeException("Cargo no encontrado"));

        Trabajador t = Trabajador.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .dni(request.getDni())
                .celular(request.getCelular())
                .correoCorporativo(request.getCorreoCorporativo())
                .empresa(empresa)
                .area(area)
                .cargo(cargo)
                .activo(request.getActivo())
                .build();

        Trabajador saved = trabajadorRepository.save(t);

        return getTrabajadorById(saved.getIdTrabajador());
    }

    @Override
    public TrabajadorResponse updateTrabajador(Integer id, TrabajadorRequest request) {
        Trabajador t = trabajadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));

        t.setNombres(request.getNombres());
        t.setApellidos(request.getApellidos());
        t.setDni(request.getDni());
        t.setCelular(request.getCelular());
        t.setCorreoCorporativo(request.getCorreoCorporativo());
        trabajadorRepository.save(t);

        return getTrabajadorById(t.getIdTrabajador());
    }

    @Override
    public void activarDesactivarTrabajador(Integer id, boolean activo) {
        Trabajador t = trabajadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
        t.setActivo(activo);
        trabajadorRepository.save(t);
    }

    @Override
    public List<ClienteResponse> getClientesByTrabajador(Integer id) {
        return trabajadorClienteRepository.findAll().stream()
                .filter(tc -> tc.getTrabajador().getIdTrabajador().equals(id))
                .map(tc -> {
                    Cliente c = tc.getCliente();
                    return ClienteResponse.builder()
                            .idCliente(c.getIdCliente())
                            .razonSocial(c.getRazonSocial())
                            .ruc(c.getRuc())
                            .activo(c.getActivo())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void asignarCliente(Integer idTrabajador, Integer idCliente) {
        Trabajador t = trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado"));
        Cliente c = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        TrabajadorCliente tc = TrabajadorCliente.builder()
                .id(new TrabajadorClienteId(idTrabajador, idCliente))
                .trabajador(t)
                .cliente(c)
                .build();
        trabajadorClienteRepository.save(tc);
    }

    @Override
    public void eliminarCliente(Integer idTrabajador, Integer idCliente) {
        TrabajadorClienteId tcId = new TrabajadorClienteId(idTrabajador, idCliente);
        trabajadorClienteRepository.deleteById(tcId);
    }
}
