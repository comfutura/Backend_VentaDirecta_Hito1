package com.backend.comfutura.repository;

import com.backend.comfutura.model.OtsTrabajador;
import com.backend.comfutura.model.OtsTrabajadorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtsTrabajadorRepository extends JpaRepository<OtsTrabajador, OtsTrabajadorId> {}

