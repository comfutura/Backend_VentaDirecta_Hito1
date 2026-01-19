package com.backend.comfutura.repository;

import com.backend.comfutura.model.OtsDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtsDetalleRepository extends JpaRepository<OtsDetalle, Integer> {}
