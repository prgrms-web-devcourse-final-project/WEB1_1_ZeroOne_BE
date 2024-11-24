package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;

import java.time.LocalDate;
import java.util.List;

public record GatheringResponse(
        Long gatheringId,
        String sort,
        String title,
        LocalDate deadLine,
        String username,
        List<String> tags

) {

    public static GatheringResponse toDto(Gathering gathering) {
        List<String> list = gathering.getGatheringTagList().stream()
                .map(gatheringTag -> gathering.getContent()).toList();

        return new GatheringResponse(gathering.getId(),gathering.getSort().name(), gathering.getTitle(), gathering.getDeadLine(), gathering.getUser().getName(), list);
    }
}
