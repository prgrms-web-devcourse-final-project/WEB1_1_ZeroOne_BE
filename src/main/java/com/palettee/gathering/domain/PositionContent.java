package com.palettee.gathering.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum PositionContent {

    DEVELOP("개발자"),
    DESIGNER("디자이너"),
    MARKETER("마케터"),
    PLANNER("기획자");

    private final String position;

    public String getPosition() {
        return position;
    }


    public static  PositionContent findPosition(String input) {
        return Arrays.stream(PositionContent.values())
                .filter(it -> it.position.equals(input))
                .findFirst()
                .orElse(null);
    }
}
