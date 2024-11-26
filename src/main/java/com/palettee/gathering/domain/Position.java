package com.palettee.gathering.domain;

import com.palettee.user.domain.MajorJobGroup;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Position {
    DEVELOP("개발자"),
    DESIGNER("디자이너"),
    PLANNER("기획자");

    private final String position;



    public static Position findPosition(String input) {
        return Arrays.stream(Position.values())
                .filter(it -> it.position.equals(input))
                .findFirst()
                .orElse(null);
    }




}
