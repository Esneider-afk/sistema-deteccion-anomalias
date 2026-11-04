package com.empresa.anomalias.controller;

import com.empresa.anomalias.model.Categoria;
import com.empresa.anomalias.model.Departamento;
import com.empresa.anomalias.model.Proveedor;
import com.empresa.anomalias.repository.CategoriaRepository;
import com.empresa.anomalias.repository.DepartamentoRepository;
import com.empresa.anomalias.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CategoriaRepository categorias;
    private final DepartamentoRepository departamentos;
    private final ProveedorRepository proveedores;

    // Las listas (categorias, departamentos, proveedores) llegan a la vista desde CatalogosAdvice
    @GetMapping
    public String vista() { return "catalogos"; }

    @PostMapping("/categorias")
    public String addCategoria(@RequestParam String nombre, RedirectAttributes ra) {
        ejecutar(ra, nombre, () -> { Categoria c = new Categoria(); c.setNombre(nombre.trim()); categorias.save(c); });
        return "redirect:/catalogos";
    }

    @PostMapping("/categorias/{id}/eliminar")
    public String delCategoria(@PathVariable Long id, RedirectAttributes ra) {
        ejecutar(ra, "x", () -> categorias.deleteById(id));
        return "redirect:/catalogos";
    }

    @PostMapping("/departamentos")
    public String addDepartamento(@RequestParam String nombre, RedirectAttributes ra) {
        ejecutar(ra, nombre, () -> { Departamento d = new Departamento(); d.setNombre(nombre.trim()); departamentos.save(d); });
        return "redirect:/catalogos";
    }

    @PostMapping("/departamentos/{id}/eliminar")
    public String delDepartamento(@PathVariable Long id, RedirectAttributes ra) {
        ejecutar(ra, "x", () -> departamentos.deleteById(id));
        return "redirect:/catalogos";
    }

    @PostMapping("/proveedores")
    public String addProveedor(@RequestParam String nombre, @RequestParam(required = false) String nit, RedirectAttributes ra) {
        ejecutar(ra, nombre, () -> { Proveedor p = new Proveedor(); p.setNombre(nombre.trim()); p.setNit(nit); proveedores.save(p); });
        return "redirect:/catalogos";
    }

    @PostMapping("/proveedores/{id}/eliminar")
    public String delProveedor(@PathVariable Long id, RedirectAttributes ra) {
        ejecutar(ra, "x", () -> proveedores.deleteById(id));
        return "redirect:/catalogos";
    }

    private void ejecutar(RedirectAttributes ra, String nombre, Runnable accion) {
        if (nombre == null || nombre.isBlank()) {
            ra.addFlashAttribute("error", "El nombre es obligatorio.");
            return;
        }
        try {
            accion.run();
            ra.addFlashAttribute("ok", "Operación realizada correctamente.");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "No se pudo completar: el nombre ya existe o el registro está asociado a gastos.");
        }
    }
}