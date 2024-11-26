package com.palettee.chat.controller.dto.request;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;

public record ChatImgUrlRequest(
        String imgUrl
) {
    public ChatImage toEntityChatImage(Chat chat) {
        return ChatImage.builder()
                .chat(chat)
                .imageUrl(this.imgUrl)
                .build();
    }
}
