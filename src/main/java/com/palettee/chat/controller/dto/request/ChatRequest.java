package com.palettee.chat.controller.dto.request;

import com.palettee.chat.domain.Chat;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;

public record ChatRequest(
        String content
) {
    public Chat toEntityChat(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .user(user)
                .chatRoom(chatRoom)
                .content(this.content)
                .build();
    }
}
