package com.palettee.chat_room.service;

import com.palettee.chat.service.ChatUserService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.exception.ChatRoomNotFoundException;
import com.palettee.chat_room.repository.ChatRoomRepository;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserService chatUserService;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse saveChatRoom(ChatRoomCreateRequest chatRoomCreateRequest) {
        ChatRoom chatRoom = chatRoomCreateRequest.toEntityChatRoom();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomResponse.of(savedChatRoom);
    }

    @Transactional
    public void participation(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User user =  getUser(userId);
        chatUserService.saveChatUser(chatRoom, user);
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> ChatRoomNotFoundException.EXCEPTION);
    }

    /**
     * 추후에 토큰에서 user 정보 꺼낼 예정
     */
    private User getUser(Long userId) {
        return userRepository.findById(userId).get();
    }
}
