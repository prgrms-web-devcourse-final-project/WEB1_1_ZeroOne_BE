package com.palettee.user.controller.dto.request.reports;

import jakarta.validation.constraints.*;

public record RegisterReportRequest(

        @NotBlank(message = "제목을 입력해 주세요.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        String content,

        @NotBlank(message = "제보 타입을 입력해 주세요.")
        String reportType
) {

}
