package com.palettee.user.controller.dto.response.reports;

import com.palettee.user.domain.*;

public record ReportResponse(
        Long reportId
) {

    public static ReportResponse of(Report report) {
        return new ReportResponse(report.getId());
    }
}
