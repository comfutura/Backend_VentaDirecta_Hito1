package com.backend.comfutura.repository;

import com.backend.comfutura.model.Fase;
import com.backend.comfutura.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaseRepository extends JpaRepository<Fase, Integer> {
}
