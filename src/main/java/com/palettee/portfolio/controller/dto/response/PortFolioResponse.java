package com.palettee.portfolio.controller.dto.response;

import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.RelatedLink;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class PortFolioResponse {
    private Long portFolioId;
    private Long userId;
    private String jobTitle;
    private String portFolioUrl;
    private String username;
    private String introduction;
    private String majorJobGroup;
    private String minorJobGroup;
    private String memberImageUrl;
    private List<String> relatedUrl;
    private boolean isLiked;

    public PortFolioResponse(Long portFolioId, Long userId, String jobTitle, String portFolioUrl, String username, String introduction, String majorJobGroup, String minorJobGroup, String memberImageUrl, List<String> relatedUrl) {
        this.portFolioId = portFolioId;
        this.userId = userId;
        this.jobTitle = jobTitle;
        this.portFolioUrl = portFolioUrl;
        this.username = username;
        this.introduction = introduction;
        this.majorJobGroup = majorJobGroup;
        this.minorJobGroup = minorJobGroup;
        this.memberImageUrl = memberImageUrl;
        this.relatedUrl = relatedUrl;
    }

    public static PortFolioResponse toDto(PortFolio portFolio) {
        List<String> relationUrl = checkRelationUrl(portFolio);
        return new PortFolioResponse(
                portFolio.getPortfolioId(),
                portFolio.getUser().getId(),
                portFolio.getUser().getJobTitle(),
                portFolio.getUrl(),
                portFolio.getUser().getName(),
                portFolio.getUser().getBriefIntro(),
                portFolio.getUser().getMajorJobGroup().name(),
                portFolio.getUser().getMinorJobGroup().name(),
                portFolio.getUser().getImageUrl(),
                relationUrl
        );
    }
    private static List<String> checkRelationUrl(PortFolio portFolio) {
        List<RelatedLink> relatedLinks = portFolio.getUser().getRelatedLinks();

        if (relatedLinks != null && !relatedLinks.isEmpty()) {
            return relatedLinks.stream()
                    .map(RelatedLink::getLink)
                    .toList();
        }
        return null;
    }
}