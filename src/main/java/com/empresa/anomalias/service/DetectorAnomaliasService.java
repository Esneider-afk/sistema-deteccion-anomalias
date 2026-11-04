package com.empresa.anomalias.service;

import com.empresa.anomalias.model.Anomalia;
import com.empresa.anomalias.model.Gasto;
import com.empresa.anomalias.repository.AnomaliaRepository;
import com.empresa.anomalias.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetectorAnomaliasService {

    public static final double Z_UMBRAL = 2.5;
    public static final int MIN_MUESTRA = 5;
    public static final double FACTOR_PROVEEDOR = 3.0;
    public static final int MIN_HISTORIAL_PROVEEDOR = 3;

    private final GastoRepository gastoRepo;
    private final AnomaliaRepository anomaliaRepo;

    /** Analiza todos los gastos y crea alertas para los que aún no tienen una. Retorna cuántas nuevas generó. */
    @Transactional
    public int analizar() {
        List<Gasto> todos = gastoRepo.findAll();
        Map<Long, List<Gasto>> porCategoria = todos.stream()
                .collect(Collectors.groupingBy(g -> g.getCategoria().getId()));
        Map<Long, List<Gasto>> porProveedor = todos.stream()
                .collect(Collectors.groupingBy(g -> g.getProveedor().getId()));

        int nuevas = 0;
        for (Gasto g : todos) {
            if (anomaliaRepo.existsByGastoId(g.getId())) continue;

            double monto = g.getMonto().doubleValue();
            List<String> criterios = new ArrayList<>();
            List<String> detalles = new ArrayList<>();
            boolean alta = false;

            // 1) Z-SCORE dentro de la categoría (excluyendo el propio gasto)
            List<Gasto> refCategoria = porCategoria.get(g.getCategoria().getId()).stream()
                    .filter(x -> !x.getId().equals(g.getId())).toList();
            if (refCategoria.size() >= MIN_MUESTRA) {
                double[] s = mediaDesviacion(refCategoria);
                if (s[1] > 0) {
                    double z = (monto - s[0]) / s[1];
                    if (z >= Z_UMBRAL) {
                        criterios.add("Z-SCORE");
                        detalles.add(String.format(
                                "Monto atípico en la categoría %s: %s frente a un promedio de %s (z = %.2f, umbral %.1f).",
                                g.getCategoria().getNombre(), moneda(monto), moneda(s[0]), z, Z_UMBRAL));
                        if (z >= 3.5) alta = true;
                    }
                }
            }

            // 2) Monto muy superior al historial del proveedor
            List<Gasto> historial = porProveedor.get(g.getProveedor().getId()).stream()
                    .filter(x -> !x.getId().equals(g.getId())).toList();
            if (historial.size() >= MIN_HISTORIAL_PROVEEDOR) {
                double prom = historial.stream().mapToDouble(x -> x.getMonto().doubleValue()).average().orElse(0);
                if (prom > 0 && monto > FACTOR_PROVEEDOR * prom) {
                    criterios.add("PROVEEDOR");
                    detalles.add(String.format(
                            "El monto %s es %.1f veces el promedio histórico del proveedor %s (%s).",
                            moneda(monto), monto / prom, g.getProveedor().getNombre(), moneda(prom)));
                }
            }

            // 3) Posible duplicado: mismo proveedor, fecha y monto
            boolean duplicado = porProveedor.get(g.getProveedor().getId()).stream().anyMatch(x ->
                    x.getId() < g.getId()
                            && x.getFecha().equals(g.getFecha())
                            && x.getMonto().compareTo(g.getMonto()) == 0);
            if (duplicado) {
                criterios.add("DUPLICADO");
                detalles.add("Existe otro gasto con el mismo proveedor, la misma fecha y el mismo monto.");
            }

            if (!criterios.isEmpty()) {
                Anomalia a = new Anomalia();
                a.setGasto(g);
                a.setCriterios(String.join(",", criterios));
                a.setDetalle(String.join("|", detalles));
                a.setSeveridad(alta || criterios.size() >= 2 ? "ALTA" : "MEDIA");
                anomaliaRepo.save(a);
                nuevas++;
            }
        }
        return nuevas;
    }

    private double[] mediaDesviacion(List<Gasto> muestra) {
        double media = muestra.stream().mapToDouble(x -> x.getMonto().doubleValue()).average().orElse(0);
        double varianza = muestra.stream()
                .mapToDouble(x -> Math.pow(x.getMonto().doubleValue() - media, 2)).average().orElse(0);
        return new double[]{media, Math.sqrt(varianza)};
    }

    private String moneda(double v) {
        return String.format("$%,.0f", v);
    }
}