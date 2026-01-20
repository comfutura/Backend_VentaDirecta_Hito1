package com.backend.comfutura.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ots {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOts;

    @Column(nullable = false, unique = true)
    private Integer ot;

    @Column(nullable = false, length = 20)
    private String ceco;

    /* ================= RELACIONES ================= */

    // OT anterior (autorelación)
    @ManyToOne
    @JoinColumn(name = "id_ots_anterior")
    private Ots otsAnterior;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_area")
    private Area area;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fase")
    private Fase fase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_site")
    private Site site;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_region")
    private Region region;

    /* ================= DATOS ================= */

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaApertura;

    private Integer diasAsignados = 0;

    private Boolean activo = true;

    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
