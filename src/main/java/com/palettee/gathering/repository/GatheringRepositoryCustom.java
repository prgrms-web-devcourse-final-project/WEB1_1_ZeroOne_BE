package com.palettee.gathering.repository;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GatheringRepositoryCustom {

    Slice<GatheringResponse> pageGathering(
            String sort,
            String period,
            String position,
            String status,
            Long gatheringId,
            Pageable pageable
    );

    CustomSliceResponse PageFindLikeGathering(Pageable pageable, Long userId , Long likeId);
}
