package com.backend.comfutura.dto.response;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaResponse {
    private Integer idArea;
    private String nombre;
    private Boolean activo;
}