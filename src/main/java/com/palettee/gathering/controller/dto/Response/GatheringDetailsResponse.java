package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.domain.GatheringTag;
import com.palettee.gathering.domain.Position;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GatheringDetailsResponse(
        Long userId,
        String sort,
        String username,
        String createTime,
        String subject,
        String contact,
        int personnel,
        String period,
        String deadLine,
        List<String> positions,
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

        List<String> positionList = gathering.getPositions()
                .stream()
                .map(Position::getContent)
                .toList();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        String deadLine = gathering.getDeadLine().format(formatter);

        return new GatheringDetailsResponse(
                gathering.getUser().getId(),
                gathering.getSort().getSort(),
                gathering.getUser().getName(),
                createTime,
                gathering.getSubject().getSubject(),
                gathering.getContact().getContact(),
                gathering.getPersonnel(),
                gathering.getPeriod(),
                deadLine,
                positionList,
                list,
                gathering.getUrl(),
                gathering.getTitle(),
                gathering.getContent()
        );
    }
}

