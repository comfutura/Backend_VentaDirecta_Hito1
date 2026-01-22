package com.backend.comfutura.service.serviceImpl;

import com.backend.comfutura.Exceptions.ResourceNotFoundException;
import com.backend.comfutura.config.security.CustomUserDetails;
import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.dto.response.OtDetailResponse;
import com.backend.comfutura.dto.response.OtFullResponse;
import com.backend.comfutura.dto.response.OtResponse;
import com.backend.comfutura.model.*;
import com.backend.comfutura.repository.*;
import com.backend.comfutura.service.OtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class OtServiceImpl implements OtService {

    private final OtsRepository otsRepository;
    private final ClienteRepository clienteRepository;
    private final AreaRepository areaRepository;
    private final ProyectoRepository proyectoRepository;
    private final FaseRepository faseRepository;
    private final SiteRepository siteRepository;
    private final RegionRepository regionRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final JefaturaClienteSolicitanteRepository jefaturaClienteSolicitanteRepository;
    private final AnalistaClienteSolicitanteRepository analistaClienteSolicitanteRepository;

    // Opcional: si más adelante necesitas EstadoOtRepository, agrégalo aquí
    // private final EstadoOtRepository estadoOtRepository;

    // LISTADO PAGINADO ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<OtResponse> listarOts(Boolean activo, Pageable pageable) {
        Page<Ots> otsPage = (activo == null)
                ? otsRepository.findAll(pageable)
                : otsRepository.findByActivo(activo, pageable);

        return otsPage.map(this::toOtResponse);
    }

    @Override
    public OtResponse obtenerPorId(Integer idOts) {
        return null;
    }

    @Override
    public OtResponse obtenerPorNumeroOt(Integer numeroOt) {
        return null;
    }

    private OtResponse toOtResponse(Ots ots) {
        int diasAsignados = 0;
        if (ots.getFechaApertura() != null) {
            long dias = ChronoUnit.DAYS.between(ots.getFechaApertura(), LocalDate.now());
            diasAsignados = (int) Math.max(dias, 0);
        }

        return OtResponse.builder()
                .idOts(ots.getIdOts())
                .ot(ots.getOt())
                .descripcion(ots.getDescripcion())
                .fechaApertura(ots.getFechaApertura())
                .diasAsignados(diasAsignados)
                .activo(ots.getActivo())
                .fechaCreacion(ots.getFechaCreacion())

                .clienteRazonSocial(ofNullable(ots.getCliente()).map(Cliente::getRazonSocial).orElse(null))
                .areaNombre(ofNullable(ots.getArea()).map(Area::getNombre).orElse(null))
                .proyectoNombre(ofNullable(ots.getProyecto()).map(Proyecto::getNombre).orElse(null))
                .faseNombre(ofNullable(ots.getFase()).map(Fase::getNombre).orElse(null))
                .siteCodigo(ofNullable(ots.getSite()).map(Site::getCodigoSitio).orElse(null))
                .regionNombre(ofNullable(ots.getRegion()).map(Region::getNombre).orElse(null))

                .jefaturaClienteSolicitante(ofNullable(ots.getJefaturaClienteSolicitante()).map(JefaturaClienteSolicitante::getDescripcion).orElse(null))
                .analistaClienteSolicitante(ofNullable(ots.getAnalistaClienteSolicitante()).map(AnalistaClienteSolicitante::getDescripcion).orElse(null))

                .creadorNombre(nombreCompleto(ots.getTrabajador()))
                .coordinadorTiCw(nombreCompleto(ots.getCoordinadorTiCw()))
                .jefaturaResponsable(nombreCompleto(ots.getJefaturaResponsable()))
                .liquidador(nombreCompleto(ots.getLiquidador()))
                .ejecutante(nombreCompleto(ots.getEjecutante()))
                .analistaContable(nombreCompleto(ots.getAnalistaContable()))

                .estadoOt(ofNullable(ots.getEstadoOt()).map(EstadoOt::getDescripcion).orElse(null))
                .build();
    }

    // DETALLE COMPLETO ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public OtDetailResponse obtenerDetallePorId(Integer idOts) {
        Ots ots = otsRepository.findByIdWithAllRelations(idOts)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada con ID: " + idOts));
        return toOtDetailResponse(ots);
    }

    

    private OtDetailResponse toOtDetailResponse(Ots ots) {
        return OtDetailResponse.builder()
                .idOts(ots.getIdOts())
                .ot(ots.getOt())
                .idOtsAnterior(ots.getIdOtsAnterior())
                .descripcion(ots.getDescripcion())
                .fechaApertura(ots.getFechaApertura())
                .fechaCreacion(ots.getFechaCreacion())
                .activo(ots.getActivo())

                .idCliente(ofNullable(ots.getCliente()).map(Cliente::getIdCliente).orElse(null))
                .clienteRazonSocial(ofNullable(ots.getCliente()).map(Cliente::getRazonSocial).orElse(null))

                .idArea(ofNullable(ots.getArea()).map(Area::getIdArea).orElse(null))
                .areaNombre(ofNullable(ots.getArea()).map(Area::getNombre).orElse(null))

                .idProyecto(ofNullable(ots.getProyecto()).map(Proyecto::getIdProyecto).orElse(null))
                .proyectoNombre(ofNullable(ots.getProyecto()).map(Proyecto::getNombre).orElse(null))

                .idFase(ofNullable(ots.getFase()).map(Fase::getIdFase).orElse(null))
                .faseNombre(ofNullable(ots.getFase()).map(Fase::getNombre).orElse(null))

                .idSite(ofNullable(ots.getSite()).map(Site::getIdSite).orElse(null))
                .siteNombre(ofNullable(ots.getSite()).map(Site::getCodigoSitio).orElse(null))

                .idRegion(ofNullable(ots.getRegion()).map(Region::getIdRegion).orElse(null))
                .regionNombre(ofNullable(ots.getRegion()).map(Region::getNombre).orElse(null))

                .idJefaturaClienteSolicitante(ofNullable(ots.getJefaturaClienteSolicitante()).map(JefaturaClienteSolicitante::getId).orElse(null))
                .jefaturaClienteSolicitanteNombre(ofNullable(ots.getJefaturaClienteSolicitante()).map(JefaturaClienteSolicitante::getDescripcion).orElse(null))

                .idAnalistaClienteSolicitante(ofNullable(ots.getAnalistaClienteSolicitante()).map(AnalistaClienteSolicitante::getId).orElse(null))
                .analistaClienteSolicitanteNombre(ofNullable(ots.getAnalistaClienteSolicitante()).map(AnalistaClienteSolicitante::getDescripcion).orElse(null))

                .idCoordinadorTiCw(ofNullable(ots.getCoordinadorTiCw()).map(Trabajador::getIdTrabajador).orElse(null))
                .coordinadorTiCwNombre(nombreCompleto(ots.getCoordinadorTiCw()))

                .idJefaturaResponsable(ofNullable(ots.getJefaturaResponsable()).map(Trabajador::getIdTrabajador).orElse(null))
                .jefaturaResponsableNombre(nombreCompleto(ots.getJefaturaResponsable()))

                .idLiquidador(ofNullable(ots.getLiquidador()).map(Trabajador::getIdTrabajador).orElse(null))
                .liquidadorNombre(nombreCompleto(ots.getLiquidador()))

                .idEjecutante(ofNullable(ots.getEjecutante()).map(Trabajador::getIdTrabajador).orElse(null))
                .ejecutanteNombre(nombreCompleto(ots.getEjecutante()))

                .idAnalistaContable(ofNullable(ots.getAnalistaContable()).map(Trabajador::getIdTrabajador).orElse(null))
                .analistaContableNombre(nombreCompleto(ots.getAnalistaContable()))

                .trabajadoresAsignados(buildTrabajadoresAsignados(ots))
                .estadoOt(ofNullable(ots.getEstadoOt()).map(EstadoOt::getDescripcion).orElse(null))
                .build();
    }

    // PARA EDICIÓN (solo IDs) ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public OtFullResponse obtenerParaEdicion(Integer idOts) {
        Ots ots = otsRepository.findById(idOts)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada con ID: " + idOts));

        return OtFullResponse.builder()
                .idOts(ots.getIdOts())
                .ot(ots.getOt())
                .idOtsAnterior(ots.getIdOtsAnterior())
                .descripcion(ots.getDescripcion())
                .fechaApertura(ots.getFechaApertura())

                .idCliente(ofNullable(ots.getCliente()).map(Cliente::getIdCliente).orElse(null))
                .idArea(ofNullable(ots.getArea()).map(Area::getIdArea).orElse(null))
                .idProyecto(ofNullable(ots.getProyecto()).map(Proyecto::getIdProyecto).orElse(null))
                .idFase(ofNullable(ots.getFase()).map(Fase::getIdFase).orElse(null))
                .idSite(ofNullable(ots.getSite()).map(Site::getIdSite).orElse(null))
                .idRegion(ofNullable(ots.getRegion()).map(Region::getIdRegion).orElse(null))

                .idJefaturaClienteSolicitante(ofNullable(ots.getJefaturaClienteSolicitante()).map(JefaturaClienteSolicitante::getId).orElse(null))
                .idAnalistaClienteSolicitante(ofNullable(ots.getAnalistaClienteSolicitante()).map(AnalistaClienteSolicitante::getId).orElse(null))

                .idCreador(ofNullable(ots.getTrabajador()).map(Trabajador::getIdTrabajador).orElse(null))
                .idCoordinadorTiCw(ofNullable(ots.getCoordinadorTiCw()).map(Trabajador::getIdTrabajador).orElse(null))
                .idJefaturaResponsable(ofNullable(ots.getJefaturaResponsable()).map(Trabajador::getIdTrabajador).orElse(null))
                .idLiquidador(ofNullable(ots.getLiquidador()).map(Trabajador::getIdTrabajador).orElse(null))
                .idEjecutante(ofNullable(ots.getEjecutante()).map(Trabajador::getIdTrabajador).orElse(null))
                .idAnalistaContable(ofNullable(ots.getAnalistaContable()).map(Trabajador::getIdTrabajador).orElse(null))

                .activo(ots.getActivo())
                .fechaCreacion(ots.getFechaCreacion())
                .build();
    }

    // CREAR / ACTUALIZAR ────────────────────────────────────────────────
    @Override
    @Transactional
    public OtDetailResponse saveOt(OtCreateRequest request) {
        Ots ots;

        if (request.getIdOts() != null) {
            // Actualización
            ots = otsRepository.findById(request.getIdOts())
                    .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada con ID: " + request.getIdOts()));
            updateOtFromRequest(ots, request);
        } else {
            // Creación
            ots = createNewOt(request);
        }

        otsRepository.save(ots);
        return toOtDetailResponse(ots);
    }

    private Ots createNewOt(OtCreateRequest req) {
        Integer ultimaOt = otsRepository.findTopByOrderByOtDesc()
                .map(Ots::getOt)
                .orElse(20250000);
        Integer nuevaOt = ultimaOt + 1;

        Integer currentTrabajadorId = getCurrentTrabajadorId();
        Trabajador creador = trabajadorRepository.findById(currentTrabajadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador autenticado no encontrado"));

        Ots ots = Ots.builder()
                .ot(nuevaOt)
                .descripcion(req.getDescripcion())
                .fechaApertura(req.getFechaApertura())
                .activo(true)
                .trabajador(creador)
                // .estadoOt(...)  ← NO lo seteamos aquí porque no tienes el repository/método
                // Opcional: si sabes el ID fijo del estado "PENDIENTE" puedes hacer:
                // .estadoOt(new EstadoOt(1))   // ← solo si aceptas ID hardcodeado
                .build();

        assignRelations(ots, req);
        return ots;
    }

    private void updateOtFromRequest(Ots ots, OtCreateRequest req) {
        if (req.getDescripcion() != null) ots.setDescripcion(req.getDescripcion());
        if (req.getFechaApertura() != null) ots.setFechaApertura(req.getFechaApertura());
        ots.setActivo(req.isActivo());

        assignRelations(ots, req);

        // Si el request trae estado nuevo → puedes agregarlo aquí cuando lo implementes
        // if (req.getIdEstadoOt() != null) {
        //     ots.setEstadoOt(estadoOtRepository.findById(req.getIdEstadoOt()).orElseThrow(...));
        // }
    }

    private void assignRelations(Ots ots, OtCreateRequest req) {
        setEntityIfPresent(req.getIdCliente(), clienteRepository, ots::setCliente);
        setEntityIfPresent(req.getIdArea(), areaRepository, ots::setArea);
        setEntityIfPresent(req.getIdProyecto(), proyectoRepository, ots::setProyecto);
        setEntityIfPresent(req.getIdFase(), faseRepository, ots::setFase);
        setEntityIfPresent(req.getIdSite(), siteRepository, ots::setSite);
        setEntityIfPresent(req.getIdRegion(), regionRepository, ots::setRegion);

        setEntityIfPresent(req.getIdJefaturaClienteSolicitante(), jefaturaClienteSolicitanteRepository, ots::setJefaturaClienteSolicitante);
        setEntityIfPresent(req.getIdAnalistaClienteSolicitante(), analistaClienteSolicitanteRepository, ots::setAnalistaClienteSolicitante);

        setTrabajadorIfPresent(req.getIdCoordinadorTiCw(), ots::setCoordinadorTiCw);
        setTrabajadorIfPresent(req.getIdJefaturaResponsable(), ots::setJefaturaResponsable);
        setTrabajadorIfPresent(req.getIdLiquidador(), ots::setLiquidador);
        setTrabajadorIfPresent(req.getIdEjecutante(), ots::setEjecutante);
        setTrabajadorIfPresent(req.getIdAnalistaContable(), ots::setAnalistaContable);
    }

    // Helpers genéricos
    private <T> void setEntityIfPresent(Integer id, JpaRepository<T, Integer> repo, java.util.function.Consumer<T> setter) {
        if (id == null) {
            setter.accept(null);
            return;
        }
        T entity = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entidad no encontrada con ID: " + id));
        setter.accept(entity);
    }

    private void setTrabajadorIfPresent(Integer id, java.util.function.Consumer<Trabajador> setter) {
        setEntityIfPresent(id, trabajadorRepository, setter);
    }

    private Integer getCurrentTrabajadorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getIdTrabajador();
        }
        throw new IllegalStateException("No hay usuario autenticado o no se encontró id_trabajador");
    }

    private String nombreCompleto(Trabajador t) {
        if (t == null) return null;
        return (ofNullable(t.getNombres()).orElse("") + " " + ofNullable(t.getApellidos()).orElse("")).trim();
    }

    private List<OtDetailResponse.TrabajadorEnOtDto> buildTrabajadoresAsignados(Ots ots) {
        List<OtDetailResponse.TrabajadorEnOtDto> list = new ArrayList<>();
        addTrabajador(list, ots.getCoordinadorTiCw(), "Coordinador TI CW");
        addTrabajador(list, ots.getJefaturaResponsable(), "Jefatura Responsable");
        addTrabajador(list, ots.getLiquidador(), "Liquidador");
        addTrabajador(list, ots.getEjecutante(), "Ejecutante");
        addTrabajador(list, ots.getAnalistaContable(), "Analista Contable");
        return list;
    }

    private void addTrabajador(List<OtDetailResponse.TrabajadorEnOtDto> list, Trabajador t, String rol) {
        if (t == null) return;
        list.add(OtDetailResponse.TrabajadorEnOtDto.builder()
                .idTrabajador(t.getIdTrabajador())
                .nombresCompletos(nombreCompleto(t))
                .cargoNombre(ofNullable(t.getCargo()).map(Cargo::getNombre).orElse(null))
                .areaTrabajadorNombre(ofNullable(t.getArea()).map(Area::getNombre).orElse(null))
                .rolEnOt(rol)
                .activo(t.getActivo())
                .build());
    }

    // TOGGLE ACTIVO ────────────────────────────────────────────────
    @Override
    @Transactional
    public void toggleActivo(Integer idOts) {
        Ots ots = otsRepository.findById(idOts)
                .orElseThrow(() -> new ResourceNotFoundException("OT no encontrada: " + idOts));
        ots.setActivo(!ots.getActivo());
        otsRepository.save(ots);
    }
}