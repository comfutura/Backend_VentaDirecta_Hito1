package com.backend.comfutura.repository;

import com.backend.comfutura.model.TrabajadorCliente;
import com.backend.comfutura.model.TrabajadorClienteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrabajadorClienteRepository extends JpaRepository<TrabajadorCliente, TrabajadorClienteId> {}