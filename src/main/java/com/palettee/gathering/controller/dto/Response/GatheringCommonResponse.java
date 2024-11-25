package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;

public record GatheringCommonResponse(

        Long gatheringId
) {

    public static GatheringCommonResponse toDTO(Gathering gathering){
        return new GatheringCommonResponse(gathering.getId());
    }
}
