package com.palettee.user.controller.dto.response.users;

import com.palettee.gathering.domain.*;

public record SimpleGatheringInfo(
        Long gatheringId,
        String title,
        String thumbnailImageUrl
) {

    public static SimpleGatheringInfo of(Gathering gathering) {
        // TODO : gathering 이미 table, entity 생기면 썸네일 이미지 넣어주기
        return new SimpleGatheringInfo(
                gathering.getId(), gathering.getTitle(), null
        );
    }
}
