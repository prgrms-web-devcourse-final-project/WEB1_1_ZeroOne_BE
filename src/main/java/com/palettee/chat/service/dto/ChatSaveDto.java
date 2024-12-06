package com.palettee.chat.service.dto;

import com.palettee.chat.controller.dto.response.ChatResponse;

import java.time.LocalDateTime;

public record ChatSaveDto(
        String chatId,
        Long userId,
        Long chatRoomId,
        String content,
        LocalDateTime sendAt
) {
    public static ChatSaveDto toDto(String chatId, ChatResponse chatResponse) {
        return new ChatSaveDto(
                chatId,
                chatResponse.getUserId(),
                chatResponse.getChatRoomId(),
                chatResponse.getContent(),
                chatResponse.getSendAt()
        );
    }
}
