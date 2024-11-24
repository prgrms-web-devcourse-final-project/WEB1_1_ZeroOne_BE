package com.palettee.gathering.repository;

import com.palettee.gathering.domain.Gathering;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryCustom {

    @Query("select distinct g from Gathering g join fetch g.user left join fetch g.gatheringTagList where g.id = :gatheringId")
    Optional<Gathering> findByGatheringId(Long gatheringId);

    @Query("select distinct  g from Gathering g join fetch g.gatheringTagList where g.id = :gatheringId")
    Optional<Gathering> findByFetchId(Long gatheringId);

    boolean existsByUserId(Long userId);

}
