package com.palettee.gathering.domain;

import com.palettee.global.exception.InvalidCategoryException;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Subject {

    DEVELOP("개발"),
    DESIGN("디자인"),
    PLAN("기획"),
    STARTUPS("창업"),
    MARKETING("마케팅"),
    HOBBY("취미"),
    FRIENDSHIP("친목"),
    LANGUAGE("어학"),
    ETC("기타");



    private final String subject;

    public String getSubject(){
        return subject;
    }

    public static Subject finSubject(final String input) {
        return Arrays.stream(Subject.values())
                .filter(it -> it.subject.equals(input))
                .findFirst()
                .orElseThrow(() -> InvalidCategoryException.EXCEPTION);
    }





    }
