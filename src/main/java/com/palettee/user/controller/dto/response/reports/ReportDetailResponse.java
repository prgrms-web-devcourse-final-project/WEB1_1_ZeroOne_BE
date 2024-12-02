package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record ReportDetailResponse(
        Long reportId,
        String title,
        String content,
        ReportType reportType,

        boolean isFixed,
        String reportDate,

        Long userId,
        String userName
) {

    public static ReportDetailResponse of(Report report) {
        return new ReportDetailResponse(
                report.getId(), report.getTitle(), report.getContent(),
                report.getReportType(), report.isFixed(), report.getCreateAt().toString(),
                report.getUser().getId(), report.getUser().getName()
        );
    }
}
