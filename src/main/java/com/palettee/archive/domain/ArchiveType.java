package com.palettee.archive.domain;

import java.util.Arrays;

public enum ArchiveType {
    RED("RED"),
    YELLOW("YELLOW"),
    GREEN("GREEN"),
    BLUE("BLUE"),
    PURPLE("PURPLE"),
    NO_COLOR("NO_COLOR");

    private final String value;

    ArchiveType(String value) {
        this.value = value;
    }

    public static ArchiveType findByInput(String input) {
        return Arrays.stream(ArchiveType.values())
                .filter(type -> type.value.equals(input))
                .findFirst()
                .orElse(NO_COLOR);
    }
}
