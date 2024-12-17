package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.gathering.controller.dto.Response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.CustomPortFolioResponse;
import org.springframework.data.domain.Pageable;

public interface PortFolioRepositoryCustom {

    CustomOffSetResponse PageFindAllPortfolio(Pageable pageable, String majorJobGroup, String minorJobGroup, String sort);

    CustomPortFolioResponse PageFindLikePortfolio(Pageable pageable, Long userId , Long likeId);
}
