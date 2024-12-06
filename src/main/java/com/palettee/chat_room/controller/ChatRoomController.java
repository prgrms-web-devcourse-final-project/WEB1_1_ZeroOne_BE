package com.palettee.chat_room.controller;

import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.service.ChatRedisService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-room")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRedisService chatRedisService;

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

    @GetMapping("/chats/{chatRoomId}")
    public ChatCustomResponse getChats(@PathVariable Long chatRoomId,
                                       @RequestParam(value = "size") int size,
                                       @RequestParam(value = "lastSendAt", required = false) LocalDateTime lastSendAt) {
        return chatRedisService.getChats(chatRoomId, size, lastSendAt);
    }
}
