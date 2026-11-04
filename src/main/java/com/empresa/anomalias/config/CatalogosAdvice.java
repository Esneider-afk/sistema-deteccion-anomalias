package com.empresa.anomalias.config;

import com.empresa.anomalias.model.Categoria;
import com.empresa.anomalias.model.Departamento;
import com.empresa.anomalias.model.Proveedor;
import com.empresa.anomalias.repository.CategoriaRepository;
import com.empresa.anomalias.repository.DepartamentoRepository;
import com.empresa.anomalias.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class CatalogosAdvice {

    private final CategoriaRepository categorias;
    private final DepartamentoRepository departamentos;
    private final ProveedorRepository proveedores;

    @ModelAttribute("categorias")
    public List<Categoria> categorias() { return categorias.findAllByOrderByNombreAsc(); }

    @ModelAttribute("departamentos")
    public List<Departamento> departamentos() { return departamentos.findAllByOrderByNombreAsc(); }

    @ModelAttribute("proveedores")
    public List<Proveedor> proveedores() { return proveedores.findAllByOrderByNombreAsc(); }
}