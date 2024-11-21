package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponseDTO;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PortFolioRepositoryCustom {

    Slice<PortFolioResponseDTO> PageFindAllPortfolio(Pageable pageable, MajorJobGroup majorJobGroup, MinorJobGroup minorJobGroup, String sort);

    CustomSliceResponse PageFindLikePortfolio(Pageable pageable, Long userId , Long likeId);
}
