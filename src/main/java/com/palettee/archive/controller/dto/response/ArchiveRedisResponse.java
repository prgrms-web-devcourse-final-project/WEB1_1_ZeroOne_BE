package com.palettee.archive.controller.dto.response;

import com.palettee.archive.domain.Archive;
import java.io.Serializable;

public record ArchiveRedisResponse(
        Long archiveId,
        String title,
        String description,
        String introduction,
        String username,
        String type,
        boolean canComment,
        long likeCount,
        boolean isLiked,
        String imageUrl,
        String createDate
) implements Serializable {

    public static ArchiveRedisResponse toResponse(Archive archive) {
        String imageUrl = archive.getArchiveImages().isEmpty() ? "" : archive.getArchiveImages().get(0).getImageUrl();

        return new ArchiveRedisResponse(
                archive.getId(),
                archive.getTitle(),
                archive.getDescription(),
                archive.getIntroduction(),
                archive.getUser().getName(),
                archive.getType().name(),
                archive.isCanComment(),
                archive.getLikeCount(),
                false,
                imageUrl,
                archive.getCreateAt().toLocalDate().toString()
        );
    }

}
