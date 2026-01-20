package com.backend.comfutura.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ots_trabajador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(OtsTrabajadorId.class)
public class OtTrabajador {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ots", nullable = false)
    private Ots ots;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trabajador", nullable = false)
    private Trabajador trabajador;

    @Id
    @Column(name = "rol_en_ot", length = 50, nullable = false)
    private String rolEnOt;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "activo")
    private Boolean activo = true;
}
