package com.empresa.anomalias.service;

import com.empresa.anomalias.dto.FilaComparacion;
import com.empresa.anomalias.dto.FilaResumen;
import com.empresa.anomalias.dto.VariacionPeriodo;
import com.empresa.anomalias.model.Gasto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EstadisticaService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public BigDecimal total(List<Gasto> gastos) {
        return gastos.stream().map(Gasto::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal promedio(List<Gasto> gastos) {
        if (gastos.isEmpty()) return BigDecimal.ZERO;
        return total(gastos).divide(BigDecimal.valueOf(gastos.size()), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal maximo(List<Gasto> gastos) {
        return gastos.stream().map(Gasto::getMonto).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
    }

    /** Agrupa por categoría, proveedor o departamento: total, cantidad, promedio y % del total. */
    public List<FilaResumen> agrupar(List<Gasto> gastos, Function<Gasto, String> clave) {
        BigDecimal granTotal = total(gastos);
        return gastos.stream().collect(Collectors.groupingBy(clave)).entrySet().stream()
                .map(e -> {
                    BigDecimal t = total(e.getValue());
                    long n = e.getValue().size();
                    BigDecimal prom = t.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
                    double pct = granTotal.signum() == 0 ? 0
                            : t.multiply(CIEN).divide(granTotal, 2, RoundingMode.HALF_UP).doubleValue();
                    return new FilaResumen(e.getKey(), t, n, prom, pct);
                })
                .sorted(Comparator.comparing(FilaResumen::getTotal).reversed())
                .toList();
    }

    /** Total por mes y variación porcentual frente al mes anterior. */
    public List<VariacionPeriodo> variacionesMensuales(List<Gasto> gastos) {
        Map<YearMonth, BigDecimal> porMes = gastos.stream().collect(Collectors.groupingBy(
                g -> YearMonth.from(g.getFecha()), TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, Gasto::getMonto, BigDecimal::add)));

        List<VariacionPeriodo> resultado = new ArrayList<>();
        BigDecimal anterior = null;
        for (Map.Entry<YearMonth, BigDecimal> e : porMes.entrySet()) {
            Double variacion = null;
            if (anterior != null && anterior.signum() != 0) {
                variacion = e.getValue().subtract(anterior).multiply(CIEN)
                        .divide(anterior, 2, RoundingMode.HALF_UP).doubleValue();
            }
            resultado.add(new VariacionPeriodo(e.getKey().toString(), e.getValue(), variacion));
            anterior = e.getValue();
        }
        return resultado;
    }

    /** Compara dos periodos (A y B) por la dimensión elegida. */
    public List<FilaComparacion> comparar(List<Gasto> periodoA, List<Gasto> periodoB, Function<Gasto, String> clave) {
        Map<String, BigDecimal> ta = totales(periodoA, clave);
        Map<String, BigDecimal> tb = totales(periodoB, clave);
        Set<String> nombres = new TreeSet<>(ta.keySet());
        nombres.addAll(tb.keySet());

        return nombres.stream().map(n -> {
            BigDecimal a = ta.getOrDefault(n, BigDecimal.ZERO);
            BigDecimal b = tb.getOrDefault(n, BigDecimal.ZERO);
            Double variacion = a.signum() == 0 ? null
                    : b.subtract(a).multiply(CIEN).divide(a, 2, RoundingMode.HALF_UP).doubleValue();
            return new FilaComparacion(n, a, b, b.subtract(a), variacion);
        }).sorted(Comparator.comparing((FilaComparacion f) -> f.getTotalA().max(f.getTotalB())).reversed())
          .toList();
    }

    private Map<String, BigDecimal> totales(List<Gasto> gastos, Function<Gasto, String> clave) {
        return gastos.stream().collect(Collectors.groupingBy(clave,
                Collectors.reducing(BigDecimal.ZERO, Gasto::getMonto, BigDecimal::add)));
    }
}