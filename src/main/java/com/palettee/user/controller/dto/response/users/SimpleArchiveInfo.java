package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;
import java.util.*;

public record SimpleArchiveInfo(
        Long archiveId,
        String title,
        ArchiveType color,
        String thumbnailImageUrl
) {

    public static SimpleArchiveInfo of(Archive archive) {
        List<ArchiveImage> images = archive.getArchiveImages();
        String thumbnail = images.stream()
                .map(ArchiveImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return new SimpleArchiveInfo(
                archive.getId(), archive.getTitle(), archive.getType(), thumbnail
        );
    }
}
