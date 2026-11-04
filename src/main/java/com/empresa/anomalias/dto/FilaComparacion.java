package com.empresa.anomalias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class FilaComparacion {
    private String nombre;
    private BigDecimal totalA;
    private BigDecimal totalB;
    private BigDecimal diferencia;
    private Double variacion;
}