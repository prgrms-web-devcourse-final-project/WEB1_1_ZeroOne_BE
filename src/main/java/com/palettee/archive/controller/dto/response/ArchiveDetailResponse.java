package com.palettee.archive.controller.dto.response;

import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.domain.Archive;
import com.palettee.user.domain.User;
import java.util.List;

public record ArchiveDetailResponse(
        String title,
        String description,
        String username,
        String type,
        boolean canComment,
        boolean isMine,
        String job,
        String userProfile,
        long likeCount,
        long commentCount,
        int hits,
        List<TagDto> tags,
        List<ImageUrlDto> imageUrls
) {

    public static ArchiveDetailResponse toResponse(
            Archive archive,
            User user,
            long likeCount,
            long count,
            List<TagDto> tagDtoList,
            List<ImageUrlDto> urlDtoList
    ) {
        User owner = archive.getUser();
        Long userId = user == null ? 0L : user.getId();
        String userProfile = owner.getImageUrl();
        return new ArchiveDetailResponse(
                archive.getTitle(),
                archive.getDescription(),
                owner.getName(),
                archive.getType().name(),
                archive.isCanComment(),
                owner.getId().equals(userId),
                owner.getMinorJobGroup().name(),
                userProfile,
                likeCount,
                count,
                archive.getHits(),
                tagDtoList,
                urlDtoList
        );
    }

}
