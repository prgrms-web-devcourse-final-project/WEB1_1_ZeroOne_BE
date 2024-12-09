package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import org.springframework.data.domain.Pageable;

public interface PortFolioRepositoryCustom {

    CustomOffSetResponse PageFindAllPortfolio(Pageable pageable, String majorJobGroup, String minorJobGroup, String sort);

    CustomSliceResponse PageFindLikePortfolio(Pageable pageable, Long userId , Long likeId);
}
