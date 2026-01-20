package com.backend.comfutura.controller;

import com.backend.comfutura.dto.request.CrearOtCompletaRequest;
import com.backend.comfutura.dto.request.OtCreateRequest;
import com.backend.comfutura.dto.response.OtResponse;
import com.backend.comfutura.service.OtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ots")
@RequiredArgsConstructor
public class OtController {

    private final OtService otService;

    // ==============================
    // CREAR OT COMPLETA
    // ==============================
    @PostMapping("/completa")
    public ResponseEntity<OtResponse> createOtCompleta(
            @Valid @RequestBody CrearOtCompletaRequest request
    ) {
        OtResponse response = otService.createOtCompleta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
