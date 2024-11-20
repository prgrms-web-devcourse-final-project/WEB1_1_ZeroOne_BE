package com.palettee.portfolio.service;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.repository.PortFolioRepository;
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
            String sort,
            String jobGroup,
            String job) {


    return null;
    }

}
