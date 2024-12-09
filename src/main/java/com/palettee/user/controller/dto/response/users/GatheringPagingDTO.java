package com.palettee.user.controller.dto.response.users;

import com.palettee.gathering.domain.*;
import java.util.*;

public record GatheringPagingDTO(
        List<Gathering> gatherings,
        boolean hasNext,
        Long nextGatheringId
) {

    public static GatheringPagingDTO of(List<Gathering> gatheringList, boolean hasNext,
            Long nextGatheringId) {

        return new GatheringPagingDTO(
                gatheringList,
                hasNext, nextGatheringId
        );
    }
}
