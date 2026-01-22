package com.backend.comfutura.controller;

import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.dto.response.OtDetailResponse;
import com.backend.comfutura.dto.response.OtFullResponse;
import com.backend.comfutura.dto.response.OtResponse;
import com.backend.comfutura.service.OtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ots")
@RequiredArgsConstructor
public class OtController {

    private final OtService otService;

    // Listado paginado
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'COORDINADOR')")   // ajusta según tus roles
    public ResponseEntity<Page<OtResponse>> listar(
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        return ResponseEntity.ok(otService.listarOts(activo, pageable));
    }

    // Detalle básico por ID (para cards o listas)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'COORDINADOR')")
    public ResponseEntity<OtResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(otService.obtenerPorId(id));
    }

    // Detalle básico por número OT
    @GetMapping("/numero/{numeroOt}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'COORDINADOR')")
    public ResponseEntity<OtResponse> obtenerPorNumeroOt(@PathVariable Integer numeroOt) {
        return ResponseEntity.ok(otService.obtenerPorNumeroOt(numeroOt));
    }

    // Crear o actualizar OT
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<OtDetailResponse> guardar(@RequestBody OtCreateRequest request) {
        OtDetailResponse response = otService.saveOt(request);
        HttpStatus status = (request.getIdOts() == null) ? HttpStatus.CREATED : HttpStatus.OK;
        return new ResponseEntity<>(response, status);
    }

    // Toggle activo/inactivo
    @PatchMapping("/{id}/toggle-activo")
    @PreAuthorize("hasRole('ADMIN')")   // o el rol que pueda desactivar OTs
    public ResponseEntity<Void> toggleActivo(@PathVariable Integer id) {
        otService.toggleActivo(id);
        return ResponseEntity.noContent().build();
    }

    // Datos para formulario de edición (solo IDs + campos editables)
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<OtFullResponse> obtenerParaEdicion(@PathVariable Integer id) {
        return ResponseEntity.ok(otService.obtenerParaEdicion(id));
    }

    // Opcional: detalle completo (incluye nombres y listas)
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'COORDINADOR')")
    public ResponseEntity<OtDetailResponse> obtenerDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(otService.obtenerDetallePorId(id));
    }
}