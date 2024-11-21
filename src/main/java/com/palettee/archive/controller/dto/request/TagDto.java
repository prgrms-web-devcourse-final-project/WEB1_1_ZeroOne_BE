package com.palettee.archive.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TagDto(

        @NotBlank(message = "공백은 입력할 수 없습니다.")
        String tag
) {
}
