package com.palettee.chat_room.controller;

import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.service.ChatRedisService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomListResponse;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.service.ChatRoomService;
import com.palettee.global.security.validation.UserUtils;
import com.palettee.user.domain.User;
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
        User contextUser = UserUtils.getContextUser();
        return chatRoomService.saveChatRoom(chatRoomCreateRequest, contextUser);
    }

    // 채팅방 참여
    @PostMapping("/participation/{chatRoomId}")
    public void participateChatRoom(@PathVariable Long chatRoomId) {
        User contextUser = UserUtils.getContextUser();
        chatRoomService.participation(chatRoomId, contextUser);
    }

    // 채팅방 나가기
    @DeleteMapping("/leave/{chatRoomId}")
    public void leaveChatRoom(@PathVariable Long chatRoomId) {
        User contextUser = UserUtils.getContextUser();
        chatRoomService.leave(chatRoomId, contextUser);
    }

    @GetMapping("/chats/{chatRoomId}")
    public ChatCustomResponse getChats(@PathVariable Long chatRoomId,
                                       @RequestParam(value = "size") int size,
                                       @RequestParam(value = "lastSendAt", required = false) LocalDateTime lastSendAt) {
        return chatRedisService.getChats(chatRoomId, size, lastSendAt);
    }

    @GetMapping("/me")
    public ChatRoomListResponse getMyChatRooms() {
        User contextUser = UserUtils.getContextUser();
        return chatRoomService.getMyChatRooms(contextUser);
    }
}
