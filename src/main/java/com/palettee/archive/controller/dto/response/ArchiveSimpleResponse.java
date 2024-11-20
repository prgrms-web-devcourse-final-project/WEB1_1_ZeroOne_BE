package com.palettee.archive.controller.dto.response;

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
}
