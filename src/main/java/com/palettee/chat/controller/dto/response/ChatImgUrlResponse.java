package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.ChatImage;

public record ChatImgUrlResponse(
        String imgUrl
) {
    public static ChatImgUrlResponse toResponse(ChatImage chatImage) {
        return new ChatImgUrlResponse(chatImage.getImageUrl());
    }
}
