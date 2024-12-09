package com.palettee.user.controller.dto.response.users;

import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.*;
import java.util.*;

public record SimpleGatheringInfo(
        Long gatheringId,
        String title,

        String sort,
        int person,
        String subject,
        String deadLine,
        List<String> tags
) {

    public static SimpleGatheringInfo of(Gathering gathering,
            GatheringTagRepository gatheringTagRepo) {
        Long id = gathering.getId();
        List<String> tags = gatheringTagRepo.findByGatheringId(id)
                .stream().map(GatheringTag::getContent)
                .toList();

        return new SimpleGatheringInfo(
                id, gathering.getTitle(),
                gathering.getSort().toString(), gathering.getPersonnel(),
                gathering.getSubject().toString(), gathering.getDeadLine().toString(),
                tags
        );
    }
}
