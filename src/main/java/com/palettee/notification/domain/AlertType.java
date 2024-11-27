package com.palettee.notification.domain;

import com.palettee.notification.exception.NotValidType;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum AlertType {
    LIKE("LIKE", "empty"),
    FEEDBACK("FEEDBACK", "/chat-room/participation/"),
    GATHERING("GATHERING", "/chat-room/participation/"),
    COFFEE_CHAT("COFFEE_CHAT", "/chat-room/participation/");

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
