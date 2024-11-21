package com.palettee.archive.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CommentWriteRequest(

        @NotBlank(message = "공백은 입력할 수 없습니다.")
        @Length(min = 1, max = 100, message = "최대 100자까지 가능합니다.")
        String content
) {
}
