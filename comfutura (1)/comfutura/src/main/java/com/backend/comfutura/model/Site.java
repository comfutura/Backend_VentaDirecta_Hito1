package com.backend.comfutura.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "site")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSite;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 255)
    private String direccion;

    private Boolean activo = true;
}
