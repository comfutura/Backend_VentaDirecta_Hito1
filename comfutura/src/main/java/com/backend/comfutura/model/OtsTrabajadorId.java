package com.backend.comfutura.model;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OtsTrabajadorId implements Serializable {
    private Integer ots;
    private Integer trabajador;
    private String rolEnOt;
}