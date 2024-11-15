package com.palettee.portfolio.repository;

import com.palettee.portfolio.domain.PortFolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortFolioRepository extends JpaRepository<PortFolio, Long> {
}
