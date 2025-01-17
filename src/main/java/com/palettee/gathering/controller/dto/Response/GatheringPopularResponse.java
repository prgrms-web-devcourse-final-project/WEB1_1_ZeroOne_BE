package com.palettee.gathering.controller.dto.Response;

import com.palettee.gathering.domain.Gathering;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GatheringPopularResponse {
    private Long gatheringId;
    private Long userId;
    private String sort;
    private int person;
    private  String subject;
    private String title;
    private  String deadLine;
    private String username;
    private List<String> tags;
    private List<String> positions;
    private Double score;
    private boolean isLiked;

    public GatheringPopularResponse(Long gatheringId, String sort, Long userId, String subject, int person, String title, List<String> positions, Double score, String deadLine, String username, List<String> tags) {
        this.gatheringId = gatheringId;
        this.sort = sort;
        this.userId = userId;
        this.subject = subject;
        this.person = person;
        this.title = title;
        this.positions = positions;
        this.score = score;
        this.deadLine = deadLine;
        this.username = username;
        this.tags = tags;
    }

    public static GatheringPopularResponse toDto(Gathering gathering, Double score) {

        String deadLine = gathering.getDeadLine().toString();

        List<String> gatheringTagList = checkGatheringTag(gathering);

        List<String> positions = gatheringPositions(gathering);


        return new GatheringPopularResponse(
                gathering.getId(),
                gathering.getSort().getSort(),
                gathering.getUser().getId(),
                gathering.getSubject().getSubject(),
                gathering.getPersonnel(),
                gathering.getTitle(),
                positions,
                score,
                deadLine,
                gathering.getUser().getName(),
                gatheringTagList
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
