package com.backend.comfutura.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ots_detalle")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ots_detalle")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ots", nullable = false)
    private Ots ots;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_maestro", nullable = false)
    private MaestroCodigo maestro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;
}
