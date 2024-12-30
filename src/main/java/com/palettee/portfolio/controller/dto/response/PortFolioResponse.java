package com.palettee.portfolio.controller.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.RelatedLink;

import java.time.LocalDateTime;
import java.util.List;

public record PortFolioResponse(
        Long portFolioId,
        Long userId,
        String jobTitle,
        String portFolioUrl,
        String username,
        String introduction,
        String majorJobGroup,
        String minorJobGroup,
        String memberImageUrl,
        List<String> relatedUrl,
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createAt
) {

    public static PortFolioResponse toDto(PortFolio portFolio){

        List<String> relationUrl=  checkRelationUrl(portFolio);

        return new PortFolioResponse(portFolio.getPortfolioId(),portFolio.getUser().getId(), portFolio.getUser().getJobTitle(), portFolio.getUrl(),portFolio.getUser().getName() , portFolio.getUser().getBriefIntro(), portFolio.getUser().getMajorJobGroup().name(), portFolio.getUser().getMinorJobGroup().name(), portFolio.getUser().getImageUrl(),relationUrl, portFolio.getCreateAt());
    }

    private static List<String> checkRelationUrl(PortFolio portFolio) {
        List<RelatedLink> relatedLinks = portFolio.getUser().getRelatedLinks();

        if(relatedLinks != null && !relatedLinks.isEmpty()){
            return relatedLinks.stream()
                    .map(RelatedLink::getLink).toList();
        }
        return null;
    }
}