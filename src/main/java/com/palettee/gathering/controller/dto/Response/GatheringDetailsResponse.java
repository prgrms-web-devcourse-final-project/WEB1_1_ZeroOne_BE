package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.domain.GatheringTag;
import com.palettee.gathering.domain.Position;
import com.palettee.gathering.domain.PositionContent;

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
        String status,
        Long likeCounts,
        List<String> positions,
        List<String> gatheringTag,
        String contactUrl,
        String title,
        String content,
        Long hits,
        String userImageUrl,
        boolean isLiked,
        boolean isHits
) {

    public static GatheringDetailsResponse toDto(Gathering gathering, Long likeCounts, Long hits,boolean isLiked, boolean isHits) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String createTime = gathering.getCreateAt().format(dateTimeFormatter);

        List<String> list = gatheringTagList(gathering);


        List<String> positionList = gatheringPositions(gathering);

        String deadLine = gathering.getDeadLine().toString();

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
                gathering.getStatus().getStatus(),
                likeCounts,
                positionList,
                list,
                gathering.getUrl(),
                gathering.getTitle(),
                gathering.getContent(),
                hits,
                gathering.getUser().getImageUrl(),
                isLiked,
                isHits
        );
    }


    private static List<String> gatheringPositions(Gathering gathering) {

        if(gathering.getPositions() != null && !gathering.getPositions().isEmpty()) {
            List<String> positionList = gathering.getPositions()
                    .stream()
                    .map(position -> position.getPositionContent().getPosition())
                    .toList();
            return positionList;
        }
        return null;
    }

    private static List<String> gatheringTagList(Gathering gathering) {

        if(gathering.getGatheringTagList() != null && !gathering.getGatheringTagList().isEmpty()){
            List<String> list = gathering.getGatheringTagList()
                    .stream()
                    .map(GatheringTag::getContent)
                    .toList();
            return list;
        }

        return null;

    }
}

