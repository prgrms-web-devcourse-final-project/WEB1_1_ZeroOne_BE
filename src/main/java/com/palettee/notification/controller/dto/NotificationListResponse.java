package com.palettee.notification.controller.dto;

import com.palettee.notification.domain.Notification;
import java.util.List;

public record NotificationListResponse(
        List<NotificationDetailResponse> notifications
) {

    public static NotificationListResponse of(List<Notification> notifications) {
        List<NotificationDetailResponse> result = notifications.stream()
                .map(NotificationDetailResponse::of)
                .toList();
        return new NotificationListResponse(result);
    }

}
