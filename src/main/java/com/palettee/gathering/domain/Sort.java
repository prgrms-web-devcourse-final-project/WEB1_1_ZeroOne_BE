package com.palettee.gathering.domain;

import com.palettee.global.exception.InvalidCategoryException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Sort {
    PROJECT("프로젝트"),
    STUDY("스터디"),
    CLUB("동아리"),
    ETC("기타");


    private final String sort;

    public String getSort(){
        return sort;
    }

    public static Sort findSort(final String input) {
        return Arrays.stream(Sort.values())
                .filter(it -> it.sort.equals(input))
                .findFirst()
                .orElseThrow(() ->InvalidCategoryException.EXCEPTION);
}
    }
