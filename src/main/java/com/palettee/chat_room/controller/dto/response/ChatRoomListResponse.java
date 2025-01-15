package com.palettee.chat_room.controller.dto.response;

import com.palettee.chat.domain.ChatUser;

import java.util.List;

public record ChatRoomListResponse(
        List<ChatRoomInfoResponse> chatRooms
) {
    public static ChatRoomListResponse toResponse(List<ChatUser> chatUsers) {
        return new ChatRoomListResponse(
                chatUsers
                        .stream()
                        .map(ChatRoomInfoResponse::toResponse)
                        .toList());
    }
}
