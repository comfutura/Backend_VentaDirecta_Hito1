package com.backend.comfutura.model;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ots_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtsDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ots_detalle")
    private Integer idOtsDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ots", nullable = false)
    private Ots ots;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maestro", nullable = false)
    private MaestroCodigo maestro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    private BigDecimal cantidad;

    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
}