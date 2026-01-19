package com.backend.comfutura.model;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

@Entity
@Table(name = "nivel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nivel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel")
    private Integer idNivel;

    @Column(unique = true)
    private String codigo;      // L1, L2, L3...

    private String nombre;
    private String descripcion;
}