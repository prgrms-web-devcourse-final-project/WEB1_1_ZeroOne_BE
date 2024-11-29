package com.palettee.chat_room.controller.dto.request;

import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull(message = "채팅방 유형 작성은 필수입니다.")
        ChatCategory chatCategory,
        Long targetId
) {
    public ChatRoom toEntityChatRoom() {
        return ChatRoom.builder()
                .chatCategory(this.chatCategory)
                .build();
    }
}
