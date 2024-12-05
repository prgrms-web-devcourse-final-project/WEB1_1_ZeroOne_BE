package com.palettee.likes.domain;

import com.palettee.gathering.domain.Contact;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum LikeType {
    PORTFOLIO("portFolio")
    , ARCHIVE("archive"),
    GATHERING("gathering");

    private final String like;

    public static LikeType findLike(final String input) {
        return Arrays.stream(LikeType.values())
                .filter(it -> it.like.equals(input))
                .findFirst()
                .orElse(null);
    }



}
