package com.palettee.gathering.repository;

import com.palettee.gathering.domain.GatheringTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GatheringTagRepository extends JpaRepository<GatheringTag, Long> {


    List<GatheringTag> findByGatheringId(Long gatheringId);
}
