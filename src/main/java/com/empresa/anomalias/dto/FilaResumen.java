package com.empresa.anomalias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class FilaResumen {
    private String nombre;
    private BigDecimal total;
    private long cantidad;
    private BigDecimal promedio;
    private double porcentaje;
}