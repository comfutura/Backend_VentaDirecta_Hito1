package com.backend.comfutura.repository;

import com.backend.comfutura.model.EstadoOt;
import com.backend.comfutura.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoOtRepository extends JpaRepository<EstadoOt, Integer> {
    Optional<EstadoOt> findByDescripcion(String descripcion);
    List<EstadoOt> findByActivoTrueOrderByDescripcionAsc();

}
