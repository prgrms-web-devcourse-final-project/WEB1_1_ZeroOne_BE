package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record ReportCommentDetailResponse(
        Long reportCommentId,
        String content,

        Long userId,
        String userName
) {

    public static ReportCommentDetailResponse of(ReportComment reportComment) {
        return new ReportCommentDetailResponse(
                reportComment.getId(), reportComment.getContent(),
                reportComment.getUser().getId(), reportComment.getUser().getName()
        );
    }
}
