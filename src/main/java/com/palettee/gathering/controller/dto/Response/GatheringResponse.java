package com.palettee.gathering.controller.dto.Response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.domain.Sort;
import com.palettee.gathering.domain.Subject;
import com.palettee.global.exception.InvalidCategoryException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class GatheringResponse {

    private Long gatheringId;
    private Long userId;
    private String sort;
    private int person;
    private String subject;
    private String title;
    private String deadLine;
    private String username;
    private boolean isLiked;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createDateTime;

    private List<String> tags;
    private List<String> positions;


    public GatheringResponse(Long gatheringId, Long userId, String sort, int person, String subject, String title, String deadLine, String username, LocalDateTime createDateTime, List<String> tags, List<String> positions) {
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.sort = sort;
        this.person = person;
        this.subject = subject;
        this.title = title;
        this.deadLine = deadLine;
        this.username = username;
        this.createDateTime = createDateTime;
        this.tags = tags;
        this.positions = positions;
    }

    public static GatheringResponse toDto(Gathering gathering) {

        String deadLine = gathering.getDeadLine().toString();

        List<String> gatheringTagList = checkGatheringTag(gathering);

        List<String> positions = gatheringPositions(gathering);

        return new GatheringResponse(
                gathering.getId(),
                gathering.getUser().getId(),
                getSort(gathering.getSort()),
                gathering.getPersonnel(),
                getSubject(gathering.getSubject()),
                gathering.getTitle(),
                deadLine,
                gathering.getUser().getName(),
                gathering.getCreateAt(),
                gatheringTagList,
                positions
        );
    }

    private static String getSort(Sort sort) {
        if (sort != null) {
            return sort.getSort();
        }
        throw InvalidCategoryException.EXCEPTION;
    }

    private static String getSubject(Subject subject) {
        if (subject != null) {
            return subject.getSubject();
        }
        throw InvalidCategoryException.EXCEPTION;
    }

    private static List<String> checkGatheringTag(Gathering gathering) {
        if (gathering.getGatheringTagList() != null && !gathering.getGatheringTagList().isEmpty()) {
            return gathering.getGatheringTagList().stream()
                    .map(gatheringTag -> gathering.getContent()).toList();
        }
        return null;
    }

    private static List<String> gatheringPositions(Gathering gathering) {
        if (gathering.getPositions() != null && !gathering.getPositions().isEmpty()) {
            return gathering.getPositions()
                    .stream()
                    .map(position -> position.getPositionContent().getPosition())
                    .toList();
        }
        return null;
    }
}