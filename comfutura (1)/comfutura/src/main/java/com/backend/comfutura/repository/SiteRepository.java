package com.backend.comfutura.repository;

import com.backend.comfutura.model.Region;
import com.backend.comfutura.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Integer> {
}
