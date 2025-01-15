package com.palettee.chat_room.controller.dto.response;

import com.palettee.chat.domain.ChatUser;

public record ChatRoomInfoResponse(
        Long chatRoomId,
        Long userId,
        String username,
        String profileImg
) {
    public static ChatRoomInfoResponse toResponse(ChatUser chatUser) {
        return new ChatRoomInfoResponse(
                chatUser.getChatRoom().getId(),
                chatUser.getUser().getId(),
                chatUser.getUser().getName(),
                chatUser.getUser().getImageUrl()
        );
    }
}
