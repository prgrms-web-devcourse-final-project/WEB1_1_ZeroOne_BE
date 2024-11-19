package com.palettee.archive.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record ArchiveUpdateRequest(
        @NotNull
        @Length(min = 1, max = 30)
        String title,

        @NotNull
        @Length(min = 1, max = 2500)
        String description,

        @NotNull
        String colorType,

        boolean canComment,

        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {
}
