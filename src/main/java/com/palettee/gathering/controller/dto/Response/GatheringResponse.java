package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record GatheringResponse(
        Long gatheringId,
        Long userId,
        String sort,
        String title,
        String deadLine,
        String username,
        List<String> tags

) {

    public static GatheringResponse toDto(Gathering gathering) {
        List<String> list = gathering.getGatheringTagList().stream()
                .map(gatheringTag -> gathering.getContent()).toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        String deadLine = gathering.getDeadLine().format(formatter);

        return new GatheringResponse(gathering.getId(),gathering.getUser().getId(), gathering.getSort().name(), gathering.getTitle(), deadLine, gathering.getUser().getName(), list);
    }
}
