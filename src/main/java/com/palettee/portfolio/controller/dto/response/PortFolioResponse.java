package com.palettee.portfolio.controller.dto.response;

import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.annotations.QueryProjection;

public record PortFolioResponse(
        Long portFolioId,
        Long userId,
        String portFolioUrl,
        String username,
        String introduction,
        String majorJobGroup,
        String minorJobGroup,
        String memberImageUrl
) {
    @QueryProjection
    public PortFolioResponse(
            Long portFolioId,
            Long userId,
            String portFolioUrl,  // 추가
            String username,
            String introduction,
            MajorJobGroup majorJobGroup,
            MinorJobGroup minorJobGroup,
            String memberImageUrl
    ) {
        this(
                portFolioId,
                userId,
                portFolioUrl,  // 추가
                username,
                introduction,
                majorJobGroup != null ? majorJobGroup.getMajorGroup() : null, // Enum 값 변환
                minorJobGroup != null ? minorJobGroup.getMinorJobGroup() : null,
                memberImageUrl
        );
    }
}
