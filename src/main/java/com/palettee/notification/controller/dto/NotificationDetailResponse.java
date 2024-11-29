package com.palettee.notification.controller.dto;

import com.palettee.notification.domain.AlertType;
import com.palettee.notification.domain.Notification;
import java.time.LocalDateTime;

public record NotificationDetailResponse(
        Long id,
        String title,
        String content,
        AlertType type,
        boolean isRead,
        String acceptUrl,
        String denyUrl,
        LocalDateTime createdAt
) {

    public static NotificationDetailResponse of(Notification notification) {
        AlertType type = notification.getType();
        return new NotificationDetailResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                type,
                notification.getIsRead(),
                type.getUrl() + notification.getChatRoomId(),
                "/notification/" + notification.getId(),
                notification.getCreateAt()
        );
    }

}
