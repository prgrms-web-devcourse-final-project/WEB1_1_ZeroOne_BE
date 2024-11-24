package com.palettee.gathering.controller.dto.Request;

import com.palettee.gathering.domain.GatheringTag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record GatheringCreateRequest(

        @NotBlank(message = "모집구분은 필수 값 입니다.")
        String sort,

        @NotBlank(message = "모집주제는 필수 값 입니다.")
        String subject,

        @NotBlank(message = "진행방식은 필수 값 입니다.")
        String contact,

        @NotNull(message = "인원 수는 필수 값입니다.")
        @Min(value = 1, message = "인원 수는 최소 1명 이상이어야 합니다.")
        int personnel,

        @NotBlank(message = "기간은 필수 값 입니다.")
        String period,

        @NotBlank(message = "마감일을 입력해주세요.")
        String deadLine,

        @NotBlank(message = "포지션을 입력해주세요.")
        String position,


        List<String> gatheringTag,

        String url,

        @NotBlank(message = "제목은 필수 값 입니다.")
        String title,

        @NotBlank(message = "내용은 필수 값 이빈다.")
        String content

) {

    public  static LocalDate getDeadLineLocalDate(String deadLine){
        return LocalDate.parse(deadLine);
    }

    public static List<GatheringTag> getGatheringTag(List<String> gatheringTag){
       return gatheringTag.stream()
                .map(GatheringTag::new).toList();
    }

}
