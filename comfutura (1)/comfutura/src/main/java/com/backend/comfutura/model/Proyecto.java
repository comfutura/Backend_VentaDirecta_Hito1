package com.backend.comfutura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProyecto;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    private Boolean activo = true;
}
