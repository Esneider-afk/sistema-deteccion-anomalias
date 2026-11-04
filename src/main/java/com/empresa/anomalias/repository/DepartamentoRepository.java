package com.empresa.anomalias.repository;

import com.empresa.anomalias.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    Optional<Departamento> findByNombreIgnoreCase(String nombre);
    List<Departamento> findAllByOrderByNombreAsc();
}