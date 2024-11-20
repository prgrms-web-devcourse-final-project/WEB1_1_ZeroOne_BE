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

    // userId는 추후에 삭제할 예정
    // 채팅방 참여
    @PostMapping("/participation/{chatRoomId}/{userId}")
    public void participateChatRoom(@PathVariable Long chatRoomId, @PathVariable Long userId) {
        chatRoomService.participation(chatRoomId, userId);
    }

    // userId는 추후에 삭제할 예정
    // 채팅방 나가기
    @DeleteMapping("/leave/{chatRoomId}/{userId}")
    public void leaveChatRoom(@PathVariable Long chatRoomId, @PathVariable Long userId) {
        chatRoomService.leave(chatRoomId, userId);
    }
}
