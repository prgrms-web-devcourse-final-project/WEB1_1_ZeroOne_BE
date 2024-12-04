package com.palettee.chat.repository;

import com.palettee.chat.controller.dto.response.ChatCustomResponse;

import java.time.LocalDateTime;

public interface ChatCustomRepository {
    ChatCustomResponse findChatNoOffset(Long chatRoomId, int size, LocalDateTime lastTimeStamp);
}
