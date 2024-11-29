package com.palettee.notification.domain;

import com.palettee.notification.exception.NotValidType;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum AlertType {
    LIKE("LIKE", "empty", "좋아요 알림", "님이 좋아요를 남겼습니다."),
    FEEDBACK("FEEDBACK", "/chat-room/participation/", "채팅 신청 알림", "님이 피드백을 요청을 했습니다."),
    GATHERING("GATHERING", "/chat-room/participation/", "채팅 신청 알림", "님이 게더링에 참여 요청을 했습니다."),
    COFFEE_CHAT("COFFEE_CHAT", "/chat-room/participation/", "채팅 신청 알림", "님이 커피챗을 신청 했습니다.");

    private final String value;
    private final String url;
    private final String title;
    private final String message;

    AlertType(String value, String url, String title, String message) {
        this.value = value;
        this.url = url;
        this.title = title;
        this.message = message;
    }

    public static AlertType findByInput(String type) {
        return Arrays.stream(AlertType.values())
                .filter(it -> it.value.equals(type))
                .findFirst()
                .orElseThrow(() -> NotValidType.EXCEPTION);
    }
}
