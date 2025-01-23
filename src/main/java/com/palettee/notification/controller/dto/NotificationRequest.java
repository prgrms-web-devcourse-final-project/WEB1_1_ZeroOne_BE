package com.palettee.notification.controller.dto;

import com.palettee.likes.domain.LikeType;
import com.palettee.notification.domain.AlertType;

public record NotificationRequest(
        Long targetId,
        String title,
        String content,
        String type,
        Long chatRoomId,
        String contentTitle,
        Long contentId,
        LikeType likeType
) {

    public static NotificationRequest like(Long targetId, String username, String contentTitle, Long contentId, LikeType likeType) {
        AlertType type = AlertType.LIKE;
        return new NotificationRequest(
                targetId,
                type.getTitle(),
                username + type.getMessage(),
                type.name(),
                null,
                contentTitle,
                contentId,
                likeType
        );
    }

    public static NotificationRequest chat(Long targetId, String username, AlertType type, Long chatRoomId) {
        return new NotificationRequest(
                targetId,
                type.getTitle(),
                username + type.getMessage(),
                type.name(),
                chatRoomId,
                null,
                null,
                null
        );
    }
}
