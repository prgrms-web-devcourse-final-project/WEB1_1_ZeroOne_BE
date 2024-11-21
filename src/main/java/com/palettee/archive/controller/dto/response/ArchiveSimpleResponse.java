package com.palettee.archive.controller.dto.response;

import com.palettee.archive.domain.Archive;
import com.palettee.likes.repository.LikeRepository;

public record ArchiveSimpleResponse(
        Long archiveId,
        String title,
        String description,
        String username,
        String type,
        boolean canComment,
        long likeCount,
        String imageUrl,
        String createDate
) {

    public static ArchiveSimpleResponse toResponse(Archive archive, LikeRepository likeRepository) {
        return new ArchiveSimpleResponse(
                archive.getId(),
                archive.getTitle(),
                archive.getDescription(),
                archive.getUser().getName(),
                archive.getType().name(),
                archive.isCanComment(),
                likeRepository.countArchiveLike(archive.getId()),
                archive.getArchiveImages().get(0).getImageUrl(),
                archive.getCreateAt().toLocalDate().toString()
        );
    }

}
