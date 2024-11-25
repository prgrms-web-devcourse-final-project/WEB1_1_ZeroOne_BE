package com.palettee.gathering.controller.dto.Response;

import com.palettee.likes.domain.Likes;

public record GatheringLikeResponse(
        Long gatheringLikeId
) {

    public static GatheringLikeResponse toDto(Likes like){
        return new GatheringLikeResponse(like.getLikeId());
    }
}
