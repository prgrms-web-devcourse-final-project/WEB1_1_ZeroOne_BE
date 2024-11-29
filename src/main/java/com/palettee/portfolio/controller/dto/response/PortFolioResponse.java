package com.palettee.portfolio.controller.dto.response;

import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.RelatedLink;

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
        List<String> relatedUrl
) {

    public static PortFolioResponse toDto(PortFolio portFolio){

        List<String> relationUrl=  checkRelationUrl(portFolio);

        return new PortFolioResponse(portFolio.getPortfolioId(),portFolio.getUser().getId(), portFolio.getUser().getJobTitle(), portFolio.getUrl(),portFolio.getUser().getName() , portFolio.getUser().getBriefIntro(), portFolio.getUser().getMajorJobGroup().name(), portFolio.getUser().getMinorJobGroup().name(), portFolio.getUser().getImageUrl(),relationUrl);
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
