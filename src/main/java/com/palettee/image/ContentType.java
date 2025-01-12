package com.palettee.image;

public enum ContentType {
    PORTFOLIO("portFolio"),
    ARCHIVE("archive"),
    GATHERING("gathering");

    private final String code;

    ContentType(String code) {
        this.code = code;
    }
}
