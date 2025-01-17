package com.palettee.portfolio.controller.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.RelatedLink;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createAt;

    public PortFolioResponse(Long portFolioId, Long userId, String jobTitle, String portFolioUrl, String username, String introduction, String majorJobGroup, String minorJobGroup, String memberImageUrl, List<String> relatedUrl, LocalDateTime createAt) {
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
        this.createAt = createAt;
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
                portFolio.getMajorJobGroup().name(),
                portFolio.getMinorJobGroup().name(),
                portFolio.getUser().getImageUrl(),
                relationUrl,
                portFolio.getCreateAt()
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