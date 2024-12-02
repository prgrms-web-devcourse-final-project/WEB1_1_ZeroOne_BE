package com.palettee.user.controller.dto.request.users;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.*;

public record RegisterPortfolioRequest(

        @NotBlank(message = "포트폴리오 링크를 제시해 주세요.")
        @Length(max = 200, message = "링크가 너무 길어 저장할 수 없습니다.")
        String portfolioUrl
) {
}
