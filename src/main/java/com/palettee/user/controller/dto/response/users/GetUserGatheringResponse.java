package com.palettee.user.controller.dto.response.users;

import com.palettee.gathering.repository.*;
import java.util.*;

public record GetUserGatheringResponse(

        List<SimpleGatheringInfo> gatherings,
        boolean hasNext,
        Long nextGatheringId
) {

    public static GetUserGatheringResponse of(GatheringPagingDTO pagingDTO,
            GatheringTagRepository gatheringTagRepo) {

        List<SimpleGatheringInfo> gatherings = pagingDTO.gatherings()
                .stream()
                .map(g -> SimpleGatheringInfo.of(g, gatheringTagRepo))
                .toList();

        return new GetUserGatheringResponse(
                gatherings,
                pagingDTO.hasNext(), pagingDTO.nextGatheringId()
        );
    }
}
