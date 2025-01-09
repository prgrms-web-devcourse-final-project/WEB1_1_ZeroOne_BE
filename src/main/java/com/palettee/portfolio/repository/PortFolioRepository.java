package com.palettee.portfolio.repository;

import com.palettee.portfolio.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PortFolioRepository extends JpaRepository<PortFolio, Long>,
        PortFolioRepositoryCustom {

    @Query("select p from PortFolio p where p.user.id = :userId")
    List<PortFolio> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from PortFolio pf where pf.user.id = :userId")
    void deleteAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update PortFolio pf set pf.hits = pf.hits + :count where pf.portfolioId = :portFolioId")
    void incrementHits(@Param("count") Long count ,@Param("portFolioId") Long portFolioId);

    @Query("select p from PortFolio p join fetch p.user where p.portfolioId in :portFolioIds")
    List<PortFolio> findAllByPortfolioIdIn(List<Long> portFolioIds);

    @Query("SELECT p FROM PortFolio p " + "JOIN FETCH p.user u " + "LEFT JOIN FETCH u.relatedLinks " + "WHERE p.portfolioId = :portFolioId")
    Optional<PortFolio> findByFetchUserPortFolio(@Param("portFolioId") Long portFolioId);


}
