package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;
import java.util.*;

public record ReportDetailResponse(
        Long reportId,
        String title,
        String content,
        ReportType reportType,

        boolean isFixed,
        String reportDate,

        Long userId,
        String userName,

        List<ReportCommentDetailResponse> reportComments
) {

    public static ReportDetailResponse of(Report report) {

        List<ReportCommentDetailResponse> comments
                = report.getReportComments()
                .stream()
                .map(ReportCommentDetailResponse::of)
                .toList();

        return new ReportDetailResponse(
                report.getId(), report.getTitle(), report.getContent(),
                report.getReportType(), report.isFixed(), report.getCreateAt().toString(),
                report.getUser().getId(), report.getUser().getName(),
                comments
        );
    }
}
