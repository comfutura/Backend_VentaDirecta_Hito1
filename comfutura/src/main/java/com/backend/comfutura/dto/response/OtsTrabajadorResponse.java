package com.backend.comfutura.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsTrabajadorResponse {
    private Integer idOts;
    private Integer idTrabajador;
    private String rolEnOt;
    private LocalDateTime fechaAsignacion;
}
