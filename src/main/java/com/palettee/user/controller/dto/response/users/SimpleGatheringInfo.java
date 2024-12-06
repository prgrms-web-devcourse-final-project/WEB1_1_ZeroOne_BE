package com.palettee.user.controller.dto.response.users;

import com.palettee.gathering.domain.*;

public record SimpleGatheringInfo(
        Long gatheringId,
        String title,
        String thumbnailImageUrl
) {

    public static SimpleGatheringInfo of(Gathering gathering) {

        String thumbnailImage = gathering.getGatheringImages()
                .stream()
                .map(GatheringImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return new SimpleGatheringInfo(
                gathering.getId(), gathering.getTitle(), thumbnailImage
        );
    }
}
