package com.palettee.portfolio.repository;

import com.palettee.portfolio.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PortFolioRepository extends JpaRepository<PortFolio, Long>,
        PortFolioRepositoryCustom {

    @Query("select p from PortFolio p where p.user.id = :userId")
    List<PortFolio> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from PortFolio pf where pf.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
