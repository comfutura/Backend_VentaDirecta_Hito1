package com.backend.comfutura.dto.response;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajadorResponse {
    private Integer idTrabajador;
    private String nombres;
    private String apellidos;
    private String dni;
    private String celular;
    private String correoCorporativo;
    private Integer idEmpresa;
    private Integer idArea;
    private Integer idCargo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}