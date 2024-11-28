package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.ChatImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatImgUrlResponse {
    private String imgUrl;

    public static ChatImgUrlResponse toResponse(ChatImage chatImage) {
        return new ChatImgUrlResponse(chatImage.getImageUrl());
    }
}
