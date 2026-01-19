package com.backend.comfutura.repository;

import com.backend.comfutura.model.MaestroCodigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaestroCodigoRepository extends JpaRepository<MaestroCodigo, Integer> {}
