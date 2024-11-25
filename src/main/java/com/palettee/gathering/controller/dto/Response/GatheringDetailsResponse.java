package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.domain.GatheringTag;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GatheringDetailsResponse(
        String sort,
        String username,
        String createTime,
        String subject,
        String contact,
        int personnel,
        String period,
        String deadLine,
        String position,
        List<String> gatheringTag,
        String contactUrl,
        String title,
        String content
) {

    public static GatheringDetailsResponse toDto(Gathering gathering) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createTime = gathering.getCreateAt().format(dateTimeFormatter);

        List<String> list = gathering.getGatheringTagList()
                .stream()
                .map(GatheringTag::getContent)
                .toList();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        String deadLine = gathering.getDeadLine().format(formatter);

        return new GatheringDetailsResponse(
                gathering.getSort().name(),
                gathering.getUser().getName(),
                createTime,
                gathering.getSubject().name(),
                gathering.getContact().name(),
                gathering.getPersonnel(),
                gathering.getPeriod(),
                deadLine,
                gathering.getPosition().name(),
                list,
                gathering.getUrl(),
                gathering.getTitle(),
                gathering.getContent()
        );
    }
}

