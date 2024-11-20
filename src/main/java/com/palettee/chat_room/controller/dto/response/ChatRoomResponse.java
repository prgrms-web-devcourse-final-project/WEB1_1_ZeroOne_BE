package com.palettee.chat_room.controller.dto.response;


import com.palettee.chat_room.domain.ChatRoom;

public record ChatRoomResponse(
        Long chatRoomId
) {
    public static ChatRoomResponse of(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId());
    }
}
