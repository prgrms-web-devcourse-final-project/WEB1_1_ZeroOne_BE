package com.palettee.portfolio.service;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.exception.PortFolioNotFoundException;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortFolioService {

    private final PortFolioRepository portFolioRepository;


    public Slice<PortFolioResponseDTO>  findAllPortFolio(
            Pageable pageable,
            MajorJobGroup majorJobGroup,
            MinorJobGroup minorJobGroup,
            String sort) {

        return portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);
    }

    @Transactional
    public void clickPortFolio(Long portPolioId){
        PortFolio portFolio = portFolioRepository.findById(portPolioId).orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);

        portFolio.incrementHits();

    }

}
