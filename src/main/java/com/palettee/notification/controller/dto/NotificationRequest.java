package com.palettee.notification.controller.dto;

public record NotificationRequest(
        Long targetId,
        String title,
        String content,
        String type,
        Long chatRoomId
) { }
