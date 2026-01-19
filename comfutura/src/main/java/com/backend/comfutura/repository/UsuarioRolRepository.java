package com.backend.comfutura.repository;

import com.backend.comfutura.model.UsuarioRol;
import com.backend.comfutura.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {}