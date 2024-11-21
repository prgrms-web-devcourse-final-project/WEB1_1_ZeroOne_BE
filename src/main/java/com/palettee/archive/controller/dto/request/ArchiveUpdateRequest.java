package com.palettee.archive.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record ArchiveUpdateRequest(
        @NotBlank(message = "제목을 입력해 주세요.")
        @Length(min = 1, max = 30, message = "최대 30자까지 가능합니다.")
        String title,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Length(min = 1, max = 2500, message = "최대 2500자까지 가능합니다.")
        String description,

        @NotBlank(message = "타입을 입력해 주세요.")
        String colorType,

        boolean canComment,

        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {
}
