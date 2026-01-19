package com.backend.comfutura.model;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(unique = true)
    private String ruc;

    @Column(name = "razon_social")
    private String razonSocial;

    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;

    private String contacto;
    private String telefono;
    private String correo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", nullable = false)
    private Banco banco;

    @Column(name = "numero_cuenta")
    private String numeroCuenta;

    private String moneda; // PEN, USD

    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}