package com.backend.comfutura.dto.response;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoResponse {
    private Integer idCargo;
    private String nombre;
    private Integer idNivel;
    private Boolean activo;
}