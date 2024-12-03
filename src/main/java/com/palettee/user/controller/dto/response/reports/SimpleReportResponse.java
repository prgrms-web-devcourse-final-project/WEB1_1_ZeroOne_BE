package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record SimpleReportResponse(
        Long reportId,
        String title,

        ReportType reportType,
        boolean isFixed,

        Long userId,
        String userName,

        String reportedAt
) {

    public static SimpleReportResponse of(Report report) {
        return new SimpleReportResponse(
                report.getId(), report.getTitle(), report.getReportType(),
                report.isFixed(), report.getUser().getId(), report.getUser().getName(),
                report.getCreateAt().toString()
        );
    }
}
