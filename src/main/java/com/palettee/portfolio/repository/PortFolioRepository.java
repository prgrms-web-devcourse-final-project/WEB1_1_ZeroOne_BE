package com.palettee.portfolio.repository;

import com.palettee.portfolio.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PortFolioRepository extends JpaRepository<PortFolio, Long>, PortFolioRepositoryCustom {

    @Modifying
    @Query("delete from PortFolio pf where pf.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
