package com.palettee.user.controller.dto.response;

import com.palettee.gathering.domain.*;
import java.util.*;

public record GetUserGatheringResponse(

        List<SimpleGatheringInfo> gatherings,
        boolean hasNext,
        Long nextGatheringId
) {

    public static GetUserGatheringResponse of(List<Gathering> gatheringList, boolean hasNext,
            Long nextGatheringId) {

        List<SimpleGatheringInfo> gatherings = gatheringList.stream()
                .map(SimpleGatheringInfo::of)
                .toList();

        return new GetUserGatheringResponse(gatherings, hasNext, nextGatheringId);
    }
}
