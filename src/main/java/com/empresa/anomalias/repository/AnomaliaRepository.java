package com.empresa.anomalias.repository;

import com.empresa.anomalias.model.Anomalia;
import com.empresa.anomalias.model.EstadoAnomalia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnomaliaRepository extends JpaRepository<Anomalia, Long> {
    boolean existsByGastoId(Long gastoId);
    List<Anomalia> findAllByOrderByFechaDeteccionDesc();
    long countByEstado(EstadoAnomalia estado);
}