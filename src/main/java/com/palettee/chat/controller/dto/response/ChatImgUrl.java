package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatImgUrl {
    private String imgUrl;

    public static ChatImgUrl toResponseFromEntity(ChatImage chatImage) {
        return new ChatImgUrl(chatImage.getImageUrl());
    }

    public ChatImage toEntityChatImage(Chat chat) {
        return ChatImage.builder()
                .chat(chat)
                .imageUrl(imgUrl)
                .build();
    }
}

