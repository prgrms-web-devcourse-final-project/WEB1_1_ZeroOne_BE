package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;

public record GatheringCreateResponse(

        Long gatheringId
) {

    public static GatheringCreateResponse toDTO(Gathering gathering){
        return new GatheringCreateResponse(gathering.getId());
    }
}
