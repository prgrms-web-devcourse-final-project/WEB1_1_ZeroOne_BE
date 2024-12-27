package com.palettee.archive.controller.dto.response;

import com.palettee.archive.domain.Archive;
import com.palettee.likes.repository.LikeRepository;
import java.io.Serializable;

public record ArchiveSimpleResponse(
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

    public static ArchiveSimpleResponse toResponse(Archive archive, Long userId, LikeRepository likeRepository) {
        String imageUrl = archive.getArchiveImages().isEmpty() ? "" : archive.getArchiveImages().get(0).getImageUrl();

        return new ArchiveSimpleResponse(
                archive.getId(),
                archive.getTitle(),
                archive.getDescription(),
                archive.getIntroduction(),
                archive.getUser().getName(),
                archive.getType().name(),
                archive.isCanComment(),
                archive.getLikeCount(),
                likeRepository.existByUserAndArchive(archive.getId(), userId).isPresent(),
                imageUrl,
                archive.getCreateAt().toLocalDate().toString()
        );
    }

}
