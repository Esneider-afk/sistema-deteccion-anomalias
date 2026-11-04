package com.empresa.anomalias.service;

import com.empresa.anomalias.dto.GastoForm;
import com.empresa.anomalias.model.Gasto;
import com.empresa.anomalias.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepo;
    private final CategoriaRepository categoriaRepo;
    private final DepartamentoRepository departamentoRepo;
    private final ProveedorRepository proveedorRepo;

    public List<Gasto> filtrar(LocalDate desde, LocalDate hasta, Long categoriaId, Long proveedorId, Long departamentoId) {
        Specification<Gasto> spec = (root, q, cb) -> cb.conjunction();
        if (desde != null)
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("fecha"), desde));
        if (hasta != null)
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("fecha"), hasta));
        if (categoriaId != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("categoria").get("id"), categoriaId));
        if (proveedorId != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("proveedor").get("id"), proveedorId));
        if (departamentoId != null)
            spec = spec.and((r, q, cb) -> cb.equal(r.get("departamento").get("id"), departamentoId));
        return gastoRepo.findAll(spec, Sort.by(Sort.Direction.DESC, "fecha", "id"));
    }

    public Gasto buscar(Long id) {
        return gastoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado"));
    }

    public GastoForm aForm(Gasto g) {
        GastoForm f = new GastoForm();
        f.setId(g.getId());
        f.setFecha(g.getFecha());
        f.setDescripcion(g.getDescripcion());
        f.setMonto(g.getMonto());
        f.setCategoriaId(g.getCategoria().getId());
        f.setDepartamentoId(g.getDepartamento().getId());
        f.setProveedorId(g.getProveedor().getId());
        return f;
    }

    @Transactional
    public void guardar(GastoForm f) {
        Gasto g = (f.getId() == null) ? new Gasto() : buscar(f.getId());
        g.setFecha(f.getFecha());
        g.setDescripcion(f.getDescripcion().trim());
        g.setMonto(f.getMonto());
        g.setCategoria(categoriaRepo.findById(f.getCategoriaId()).orElseThrow());
        g.setDepartamento(departamentoRepo.findById(f.getDepartamentoId()).orElseThrow());
        g.setProveedor(proveedorRepo.findById(f.getProveedorId()).orElseThrow());
        gastoRepo.save(g);
    }

    public void eliminar(Long id) {
        gastoRepo.deleteById(id);
    }
}