package com.palettee.gathering.controller.dto.Response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.gathering.domain.Gathering;

import java.time.LocalDateTime;
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
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createDateTime,
        List<String> tags,
        List<String> positions

) {

    public static GatheringResponse toDto(Gathering gathering) {

        String deadLine = gathering.getDeadLine().toString();

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
                gathering.getCreateAt(),
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
