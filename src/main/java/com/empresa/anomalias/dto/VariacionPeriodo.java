package com.empresa.anomalias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class VariacionPeriodo {
    private String periodo;
    private BigDecimal total;
    private Double variacion; // % respecto al periodo anterior (null en el primero)
}