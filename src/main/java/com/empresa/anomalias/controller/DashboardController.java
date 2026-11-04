package com.empresa.anomalias.controller;

import com.empresa.anomalias.model.EstadoAnomalia;
import com.empresa.anomalias.model.Gasto;
import com.empresa.anomalias.repository.AnomaliaRepository;
import com.empresa.anomalias.service.EstadisticaService;
import com.empresa.anomalias.service.GastoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final GastoService gastoService;
    private final EstadisticaService est;
    private final AnomaliaRepository anomaliaRepo;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long departamentoId,
            Model m) {

        List<Gasto> gastos = gastoService.filtrar(desde, hasta, categoriaId, proveedorId, departamentoId);

        m.addAttribute("desde", desde);
        m.addAttribute("hasta", hasta);
        m.addAttribute("categoriaId", categoriaId);
        m.addAttribute("proveedorId", proveedorId);
        m.addAttribute("departamentoId", departamentoId);

        m.addAttribute("total", est.total(gastos));
        m.addAttribute("promedio", est.promedio(gastos));
        m.addAttribute("maximo", est.maximo(gastos));
        m.addAttribute("cantidad", gastos.size());
        m.addAttribute("pendientes", anomaliaRepo.countByEstado(EstadoAnomalia.PENDIENTE_REVISION));

        m.addAttribute("porCategoria", est.agrupar(gastos, g -> g.getCategoria().getNombre()));
        m.addAttribute("porProveedor", est.agrupar(gastos, g -> g.getProveedor().getNombre()));
        m.addAttribute("porDepartamento", est.agrupar(gastos, g -> g.getDepartamento().getNombre()));
        m.addAttribute("variaciones", est.variacionesMensuales(gastos));
        return "dashboard";
    }

    @GetMapping("/comparar")
    public String comparar(
            @RequestParam(defaultValue = "categoria") String dimension,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desdeA,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hastaA,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desdeB,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hastaB,
            Model m) {

        // Por defecto: A = mes anterior, B = mes actual
        LocalDate hoy = LocalDate.now();
        if (desdeB == null) desdeB = hoy.withDayOfMonth(1);
        if (hastaB == null) hastaB = hoy;
        if (desdeA == null) desdeA = desdeB.minusMonths(1);
        if (hastaA == null) hastaA = desdeB.minusDays(1);

        Function<Gasto, String> clave = switch (dimension) {
            case "proveedor" -> g -> g.getProveedor().getNombre();
            case "departamento" -> g -> g.getDepartamento().getNombre();
            default -> g -> g.getCategoria().getNombre();
        };

        List<Gasto> a = gastoService.filtrar(desdeA, hastaA, null, null, null);
        List<Gasto> b = gastoService.filtrar(desdeB, hastaB, null, null, null);

        m.addAttribute("dimension", dimension);
        m.addAttribute("desdeA", desdeA);
        m.addAttribute("hastaA", hastaA);
        m.addAttribute("desdeB", desdeB);
        m.addAttribute("hastaB", hastaB);
        m.addAttribute("totalA", est.total(a));
        m.addAttribute("totalB", est.total(b));
        m.addAttribute("filas", est.comparar(a, b, clave));
        return "comparar";
    }
}