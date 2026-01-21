package com.backend.comfutura.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinadorTiCwRequest {

    private Integer id; // null = crear | con valor = actualizar

    @NotBlank
    private String descripcion;

    private Boolean activo;
}
