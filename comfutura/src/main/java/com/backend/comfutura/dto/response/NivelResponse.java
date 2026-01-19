package com.backend.comfutura.dto.response;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelResponse {
    private Integer idNivel;
    private String codigo;
    private String nombre;
    private String descripcion;
}
