package com.empresa.anomalias.controller;

import com.empresa.anomalias.model.Anomalia;
import com.empresa.anomalias.model.EstadoAnomalia;
import com.empresa.anomalias.repository.AnomaliaRepository;
import com.empresa.anomalias.service.DetectorAnomaliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/anomalias")
@RequiredArgsConstructor
public class AnomaliaController {

    private final AnomaliaRepository repo;
    private final DetectorAnomaliasService detector;

    /** Parámetros de los criterios, visibles en la lista y en el detalle. */
    @ModelAttribute
    public void criterios(Model m) {
        m.addAttribute("zUmbral", DetectorAnomaliasService.Z_UMBRAL);
        m.addAttribute("minMuestra", DetectorAnomaliasService.MIN_MUESTRA);
        m.addAttribute("factorProveedor", DetectorAnomaliasService.FACTOR_PROVEEDOR);
        m.addAttribute("minHistorial", DetectorAnomaliasService.MIN_HISTORIAL_PROVEEDOR);
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoAnomalia estado,
                         @RequestParam(required = false) String severidad, Model m) {
        List<Anomalia> lista = repo.findAllByOrderByFechaDeteccionDesc().stream()
                .filter(a -> estado == null || a.getEstado() == estado)
                .filter(a -> severidad == null || severidad.isBlank() || a.getSeveridad().equals(severidad))
                .toList();

        m.addAttribute("anomalias", lista);
        m.addAttribute("total", repo.count());
        m.addAttribute("pendientes", repo.countByEstado(EstadoAnomalia.PENDIENTE_REVISION));
        m.addAttribute("justificadas", repo.countByEstado(EstadoAnomalia.JUSTIFICADA));
        m.addAttribute("revisadas", repo.countByEstado(EstadoAnomalia.REVISADA));
        m.addAttribute("estados", EstadoAnomalia.values());
        m.addAttribute("estadoSel", estado);
        m.addAttribute("severidadSel", severidad);
        return "anomalias/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model m) {
        m.addAttribute("a", repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Anomalía no encontrada")));
        m.addAttribute("estados", EstadoAnomalia.values());
        return "anomalias/detalle";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam EstadoAnomalia estado,
                                @RequestParam(required = false) String justificacion, RedirectAttributes ra) {
        if (estado == EstadoAnomalia.JUSTIFICADA && (justificacion == null || justificacion.isBlank())) {
            ra.addFlashAttribute("error", "Para marcarla como justificada debes escribir la justificación.");
            return "redirect:/anomalias/" + id;
        }
        Anomalia a = repo.findById(id).orElseThrow();
        a.setEstado(estado);
        a.setJustificacion(justificacion == null ? null : justificacion.trim());
        repo.save(a);
        ra.addFlashAttribute("ok", "Estado actualizado: " + estado.getEtiqueta());
        return "redirect:/anomalias/" + id;
    }

    @PostMapping("/analizar")
    public String analizar(RedirectAttributes ra) {
        int n = detector.analizar();
        ra.addFlashAttribute("ok", n > 0 ? "Análisis completado: " + n + " nueva(s) anomalía(s)." : "Análisis completado: no se encontraron nuevas anomalías.");
        return "redirect:/anomalias";
    }
}