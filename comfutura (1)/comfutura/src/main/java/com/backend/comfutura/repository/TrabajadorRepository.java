package com.backend.comfutura.repository;

import com.backend.comfutura.model.Site;
import com.backend.comfutura.model.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Integer> {
}
