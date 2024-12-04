package com.palettee.archive.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.*;
import org.hibernate.validator.constraints.*;

public record ArchiveRegisterRequest(

        @NotBlank(message = "제목을 입력해 주세요.")
        @Length(max = 30, message = "최대 30자까지 가능합니다.")
        String title,

        @NotBlank(message = "요약을 입력해 주세요.")
        @Length(max = 2500, message = "최대 2500자까지 가능합니다.")
        String description,

        @NotBlank(message = "내용을 입력해 주세요.")
        @Length(max = 2500, message = "최대 2500자까지 가능합니다.")
        String introduction,

        @NotBlank(message = "타입을 입력해 주세요.")
        String colorType,

        boolean canComment,

        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {
}
