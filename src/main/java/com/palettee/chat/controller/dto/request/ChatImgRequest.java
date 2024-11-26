package com.palettee.chat.controller.dto.request;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;

import java.util.List;

public record ChatImgRequest(
        List<ChatImgUrlRequest> imgUrls
) {
    public Chat toEntityChat(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }

    public List<ChatImage> toEntityChatImages(Chat chat) {
        return imgUrls.stream()
                .map(chatImgUrlRequest -> chatImgUrlRequest.toEntityChatImage(chat))
                .toList();
    }
}
