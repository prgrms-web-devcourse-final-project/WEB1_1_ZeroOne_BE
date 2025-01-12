//package com.palettee.user.controller.dto.response.users;
//
//import com.palettee.archive.domain.*;
//import java.util.*;
//
//public record SimpleArchiveInfo(
//        Long archiveId,
//        String title,
//        String type,
//        String imageUrl,
//
//        String description,
//        String introduction,
//        boolean canComment,
//        String createDate
//) {
//
//    public static SimpleArchiveInfo of(Archive archive) {
//        List<ArchiveImage> images = archive.getArchiveImages();
//        String imageUrl = images.stream()
//                .map(ArchiveImage::getImageUrl)
//                .findFirst()
//                .orElse(null);
//
//        ArchiveType type = archive.getType();
//
//        return new SimpleArchiveInfo(
//                archive.getId(), archive.getTitle(),
//                !type.equals(ArchiveType.NO_COLOR) ? type.toString() : "DEFAULT",
//                imageUrl,
//                archive.getDescription(), archive.getIntroduction(),
//                archive.isCanComment(), archive.getCreateAt().toString()
//        );
//    }
//}
