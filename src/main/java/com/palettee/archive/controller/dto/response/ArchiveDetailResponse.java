package com.palettee.archive.controller.dto.response;

import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.domain.Archive;
import java.util.List;

public record ArchiveDetailResponse(
        String title,
        String description,
        String username,
        String type,
        boolean canComment,
        boolean isMine,
        String job,
        long likeCount,
        long commentCount,
        int hits,
        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {

    public static ArchiveDetailResponse toResponse(
            Archive archive,
            Long userId,
            long likeCount,
            long count,
            List<TagDto> tagDtoList,
            List<ImageUrlDto> urlDtoList
    ) {
        return new ArchiveDetailResponse(
                archive.getTitle(),
                archive.getDescription(),
                archive.getUser().getName(),
                archive.getType().name(),
                archive.isCanComment(),
                archive.getUser().getId().equals(userId),
                archive.getUser().getMinorJobGroup().name(),
                likeCount,
                count,
                archive.getHits(),
                tagDtoList,
                urlDtoList
        );
    }

}
