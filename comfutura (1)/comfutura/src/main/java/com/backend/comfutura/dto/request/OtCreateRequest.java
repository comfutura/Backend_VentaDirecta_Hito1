package com.backend.comfutura.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OtCreateRequest {

    @NotNull
    @Positive
    private Integer ot;

    @NotBlank
    @Size(max = 20)
    private String ceco;

    private Integer idOtsAnterior;

    @NotNull
    private Integer idCliente;

    @NotNull
    private Integer idArea;

    @NotNull
    private Integer idProyecto;

    @NotNull
    private Integer idFase;

    @NotNull
    private Integer idSite;

    @NotNull
    private Integer idRegion;

    @Size(max = 1000)
    private String descripcion;

    @NotNull
    private LocalDate fechaApertura;

    private Integer diasAsignados;
}
