package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PortFolioRepositoryCustom {

    Slice<PortFolioResponseDTO> PageFinAllPortfolio(Pageable pageable,String sort, String jobGroup, String job);
}
