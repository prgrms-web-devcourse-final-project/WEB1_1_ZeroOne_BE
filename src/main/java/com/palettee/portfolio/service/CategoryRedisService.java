package com.palettee.portfolio.service;

import com.palettee.gathering.controller.dto.Response.GatheringPopularResponse;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.portfolio.controller.dto.response.PortFolioPopularResponse;
import com.palettee.portfolio.repository.PortFolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryRedisService {

    private final PortFolioRepository portFolioRepository;

    private final GatheringRepository gatheringRepository;

    public void incrementPfHits(Long count, Long portFolioId){
        portFolioRepository.incrementHits(count, portFolioId);
    }

    public void incrementGatheringHits(Long count, Long gatheringId){
        gatheringRepository.incrementHits(count, gatheringId);
    }

    public List<GatheringPopularResponse> getPopularGathering(List<Long> topRankTargetIds, Map<Long, Double> combinedScores){
        return gatheringRepository.findByUserIds(topRankTargetIds)
                .stream()
                .map(gathering -> GatheringPopularResponse.toDto(gathering, combinedScores.get(gathering.getId())))
                .collect(Collectors.toList());
    }

    public List<PortFolioPopularResponse> getPopularPortFolio(List<Long> topRankTargetIds, Map<Long, Double> combinedScores){
        return portFolioRepository.findAllByPortfolioIdIn(topRankTargetIds)
                .stream()
                .map(portFolio -> PortFolioPopularResponse.toDto(portFolio, combinedScores.get(portFolio.getPortfolioId())))
                .collect(Collectors.toList());
    }


}
