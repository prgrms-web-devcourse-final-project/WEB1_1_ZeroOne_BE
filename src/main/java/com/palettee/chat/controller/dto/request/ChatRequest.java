package com.palettee.chat.controller.dto.request;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;

import java.util.List;

public record ChatRequest(
        String content,
        List<ChatImgUrlRequest> imgUrls
) {
    public Chat toEntityChat(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .user(user)
                .chatRoom(chatRoom)
                .content(this.content)
                .build();
    }

    public List<ChatImage> toEntityChatImages(Chat chat) {
        return imgUrls.stream()
                .map(chatImgUrlRequest -> chatImgUrlRequest.toEntityChatImage(chat))
                .toList();
    }
}
