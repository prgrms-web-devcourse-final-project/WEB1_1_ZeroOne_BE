package com.palettee.user.controller.dto.request.reports;

import jakarta.validation.constraints.*;

public record RegisterReportCommentRequest(

        @NotBlank(message = "댓글 내용을 입력해 주세요.")
        String content
) {

}
