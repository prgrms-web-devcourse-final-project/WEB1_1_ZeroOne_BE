package com.palettee.archive.controller.dto.response;

import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import java.util.List;

public record ArchiveSimpleResponse(
        String title,
        String description,
        String username,
        String type,
        boolean canComment,
        String job,
        int likeCount,
        int commentCount,
        int hits,
        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {
}
