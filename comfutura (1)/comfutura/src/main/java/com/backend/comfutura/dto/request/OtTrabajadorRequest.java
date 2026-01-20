package com.backend.comfutura.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OtTrabajadorRequest {

    @NotNull
    private Integer idTrabajador;

    @NotBlank
    @Size(max = 50)
    private String rolEnOt;
}
