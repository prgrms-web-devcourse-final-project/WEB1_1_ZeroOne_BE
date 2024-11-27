package com.palettee.gathering.repository;

import com.palettee.gathering.domain.GatheringImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GatheringImageRepository extends JpaRepository<GatheringImage, Long> {

    List<GatheringImage> findByGatheringId(Long gatheringId);
}
