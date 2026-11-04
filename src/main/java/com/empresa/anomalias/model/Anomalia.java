package com.empresa.anomalias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Anomalia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "gasto_id", unique = true)
    private Gasto gasto;

    /** Criterios separados por coma: Z-SCORE,PROVEEDOR,DUPLICADO */
    @Column(nullable = false)
    private String criterios;

    /** Explicaciones separadas por "|" */
    @Column(length = 2000)
    private String detalle;

    /** ALTA o MEDIA */
    @Column(nullable = false)
    private String severidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAnomalia estado = EstadoAnomalia.PENDIENTE_REVISION;

    private LocalDateTime fechaDeteccion = LocalDateTime.now();

    @Column(length = 1000)
    private String justificacion;
}