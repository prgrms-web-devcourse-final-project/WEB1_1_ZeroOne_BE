package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;

public record SimpleArchiveInfo(
        Long archiveId,
        String title,
        String type,
        String imageUrl,

        String description,
        String introduction,
        boolean canComment,
        String createDate
) {

    public static SimpleArchiveInfo of(Archive archive, String thumbnail) {

        ArchiveType type = archive.getType();

        return new SimpleArchiveInfo(
                archive.getId(), archive.getTitle(),
                !type.equals(ArchiveType.NO_COLOR) ? type.toString() : "DEFAULT",
                thumbnail,
                archive.getDescription(), archive.getIntroduction(),
                archive.isCanComment(), archive.getCreateAt().toString()
        );
    }
}
