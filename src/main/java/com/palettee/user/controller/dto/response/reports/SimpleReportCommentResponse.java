package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record SimpleReportCommentResponse(
        Long reportCommentId,
        String content,

        Long userId,
        String userName
) {

    public static SimpleReportCommentResponse of(ReportComment reportComment) {
        return new SimpleReportCommentResponse(
                reportComment.getId(), reportComment.getContent(),
                reportComment.getUser().getId(), reportComment.getUser().getName()
        );
    }
}
