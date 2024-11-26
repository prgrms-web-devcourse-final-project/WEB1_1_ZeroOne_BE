package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.Chat;

import java.time.LocalDateTime;
import java.util.List;

public record ChatImgResponse(
        Long chatId,
        String email,
        String profileImg,
        List<ChatImgUrlResponse> imgUrls,
        LocalDateTime timestamp
) {
    public static ChatImgResponse toResponse(Chat chat) {
        return new ChatImgResponse(
                chat.getId(),
                chat.getUser().getEmail(),
                chat.getUser().getImageUrl(),
                chat.getChatImages().stream()
                        .map(ChatImgUrlResponse::toResponse)
                        .toList(),
                chat.getCreateAt()
        );
    }
}
