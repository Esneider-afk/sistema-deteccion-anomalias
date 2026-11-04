package com.empresa.anomalias.controller;

import com.empresa.anomalias.dto.GastoForm;
import com.empresa.anomalias.model.Gasto;
import com.empresa.anomalias.service.DetectorAnomaliasService;
import com.empresa.anomalias.service.EstadisticaService;
import com.empresa.anomalias.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;
    private final EstadisticaService est;
    private final DetectorAnomaliasService detector;

    @GetMapping
    public String listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long departamentoId,
            Model m) {

        List<Gasto> gastos = gastoService.filtrar(desde, hasta, categoriaId, proveedorId, departamentoId);
        m.addAttribute("gastos", gastos);
        m.addAttribute("total", est.total(gastos));
        m.addAttribute("promedio", est.promedio(gastos));
        m.addAttribute("desde", desde);
        m.addAttribute("hasta", hasta);
        m.addAttribute("categoriaId", categoriaId);
        m.addAttribute("proveedorId", proveedorId);
        m.addAttribute("departamentoId", departamentoId);
        return "gastos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("form", new GastoForm());
        return "gastos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        m.addAttribute("form", gastoService.aForm(gastoService.buscar(id)));
        return "gastos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("form") GastoForm form, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) return "gastos/form";
        gastoService.guardar(form);
        int nuevas = detector.analizar();
        ra.addFlashAttribute("ok", nuevas > 0
                ? "Gasto guardado. Se generó " + nuevas + " alerta(s) de anomalía."
                : "Gasto guardado correctamente.");
        return "redirect:/gastos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        gastoService.eliminar(id);
        ra.addFlashAttribute("ok", "Gasto eliminado correctamente.");
        return "redirect:/gastos";
    }
}