package com.palettee.notification.domain;

import com.palettee.notification.exception.NotValidType;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum AlertType {
    BOOKMARK("BOOKMARK", "empty"),
    FEEDBACK("FEEDBACK", ""),
    GATHERING("GATHERING", "");

    private final String value;
    private final String url;

    AlertType(String value, String url) {
        this.value = value;
        this.url = url;
    }

    public static AlertType findByInput(String type) {
        return Arrays.stream(AlertType.values())
                .filter(it -> it.value.equals(type))
                .findFirst()
                .orElseThrow(() -> NotValidType.EXCEPTION);
    }
}
