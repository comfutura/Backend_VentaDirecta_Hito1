package com.backend.comfutura.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Clase auxiliar para clave compuesta (si usas @IdClass)
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TrabajadorClienteId implements Serializable {
    private Integer trabajador;
    private Integer cliente;
}
