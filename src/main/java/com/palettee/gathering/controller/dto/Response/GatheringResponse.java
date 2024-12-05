package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record GatheringResponse(
        Long gatheringId,
        Long userId,
        String sort,
        int person,
        String subject,
        String title,
        String deadLine,
        String username,
        List<String> tags,
        List<String> positions

) {

    public static GatheringResponse toDto(Gathering gathering) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        String deadLine = gathering.getDeadLine().format(formatter);

        List<String> gatheringTagList = checkGatheringTag(gathering);

        List<String> positions = gatheringPositions(gathering);


        return new GatheringResponse(
                gathering.getId(),
                gathering.getUser().getId(),
                gathering.getSort().getSort(),
                gathering.getPersonnel(),
                gathering.getSubject().getSubject(),
                gathering.getTitle(),
                deadLine,
                gathering.getUser().getName(),
                gatheringTagList,
                positions
        );
    }


    private static List<String> checkGatheringTag(Gathering gathering) {
        if(gathering.getGatheringTagList() != null && !gathering.getGatheringTagList().isEmpty()){
          return gathering.getGatheringTagList().stream()
                    .map(gatheringTag -> gathering.getContent()).toList();
        }
        return null;
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

}
