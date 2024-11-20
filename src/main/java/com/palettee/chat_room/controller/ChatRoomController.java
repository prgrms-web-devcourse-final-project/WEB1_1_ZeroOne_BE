package com.palettee.chat_room.controller;

import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-room")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 채팅방 생성
    @PostMapping
    public ChatRoomResponse createChatRoom(@Valid @RequestBody ChatRoomCreateRequest chatRoomCreateRequest) {
        return chatRoomService.saveChatRoom(chatRoomCreateRequest);
    }
}
