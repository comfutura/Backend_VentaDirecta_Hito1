package com.backend.comfutura.repository;

import com.backend.comfutura.model.Proyecto;
import com.backend.comfutura.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepository extends JpaRepository<Proyecto, Integer> {
}
