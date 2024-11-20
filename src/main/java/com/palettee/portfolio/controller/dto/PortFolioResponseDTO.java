package com.palettee.portfolio.controller.dto;

import com.palettee.portfolio.domain.PortFolio;
import com.querydsl.core.annotations.QueryProjection;

public record PortFolioResponseDTO (
        Long portFolioId,
        String username,
//        int likeCounts,
//        int hints,
        String introduction,
//        String mainJobGroup,
        String memberImageUrl
){
    @QueryProjection
    public PortFolioResponseDTO(
            Long portFolioId,
            String username,
            String introduction,
            String memberImageUrl
    ) {
        this.portFolioId = portFolioId;
        this.username = username;
        this.introduction = introduction;
        this.memberImageUrl = memberImageUrl;
    }


    public static PortFolioResponseDTO toDTO(PortFolio portFolio){
        return new PortFolioResponseDTO(
                portFolio.getPortfolioId(),
                portFolio.getUser().getName(),
                portFolio.getUser().getBriefIntro(),
                portFolio.getUrl());
    }
}
