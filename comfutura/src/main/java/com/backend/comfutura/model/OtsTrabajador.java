package com.backend.comfutura.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ots_trabajador")
@IdClass(OtsTrabajadorId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsTrabajador {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ots", nullable = false)
    private Ots ots;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_trabajador", nullable = false)
    private Trabajador trabajador;

    @Id
    @Column(name = "rol_en_ot")
    private String rolEnOt;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion = LocalDateTime.now();
}