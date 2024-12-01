package com.palettee.portfolio.service;

import com.palettee.portfolio.repository.PortFolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PortFolioRedisService {

    private final PortFolioRepository portFolioRepository;

    public void incrementHits(Long count, Long portFolioId){
        portFolioRepository.incrementHits(count, portFolioId);
    }


}
