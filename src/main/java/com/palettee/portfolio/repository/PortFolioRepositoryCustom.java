package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PortFolioRepositoryCustom {

    Slice<PortFolioResponse> PageFindAllPortfolio(Pageable pageable, String majorJobGroup, String minorJobGroup, String sort);

    CustomSliceResponse PageFindLikePortfolio(Pageable pageable, Long userId , Long likeId);
}
