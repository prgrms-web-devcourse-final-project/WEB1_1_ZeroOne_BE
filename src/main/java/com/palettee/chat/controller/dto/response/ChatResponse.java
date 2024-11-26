package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.Chat;

import java.time.LocalDateTime;

public record ChatResponse(
        Long chatId,
        String email,
        String profileImg,
        String content,
        LocalDateTime timestamp
) {
    public static ChatResponse toResponse(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getUser().getEmail(),
                chat.getUser().getImageUrl(),
                chat.getContent(),
                chat.getCreateAt()
        );
    }
}
