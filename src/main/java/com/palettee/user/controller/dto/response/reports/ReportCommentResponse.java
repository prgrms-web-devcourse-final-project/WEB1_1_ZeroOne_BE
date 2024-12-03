package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record ReportCommentResponse(
        Long reportCommentId
) {

    public static ReportCommentResponse of(ReportComment reportComment) {
        return new ReportCommentResponse(reportComment.getId());
    }
}
