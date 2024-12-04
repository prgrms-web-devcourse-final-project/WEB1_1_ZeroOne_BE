package com.palettee.gathering.domain;

import com.palettee.global.exception.InvalidCategoryException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Status {

    ONGOING("모집중"),
    EXPIRED("기간만료"),
    COMPLETE("모집완료");

    private final String status;

    public String getStatus(){
        return status;
    }

    public static Status findsStatus(final String input) {
        return Arrays.stream(Status.values())
                .filter(it -> it.status.equals(input))
                .findFirst()
                .orElseThrow(() -> InvalidCategoryException.EXCEPTION);
    }

}
