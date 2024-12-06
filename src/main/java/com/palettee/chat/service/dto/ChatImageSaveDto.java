package com.palettee.chat.service.dto;

public record ChatImageSaveDto(
        String chatId,
        String imgUrl
) {
    public static ChatImageSaveDto toDto(String chatId, String imgUrl) {
        return new ChatImageSaveDto(
                chatId,
                imgUrl
        );
    }
}
