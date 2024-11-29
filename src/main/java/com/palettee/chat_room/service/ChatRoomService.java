package com.palettee.chat_room.service;

import com.palettee.chat.service.ChatUserService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.exception.ChatRoomNotFoundException;
import com.palettee.chat_room.repository.ChatRoomRepository;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.domain.AlertType;
import com.palettee.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    @Transactional
    public ChatRoomResponse saveChatRoom(ChatRoomCreateRequest chatRoomCreateRequest) {
        ChatRoom chatRoom = chatRoomCreateRequest.toEntityChatRoom();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        sendNotification(chatRoomCreateRequest, savedChatRoom);
        return ChatRoomResponse.of(savedChatRoom);
    }

    private void sendNotification(ChatRoomCreateRequest chatRoomCreateRequest, ChatRoom savedChatRoom) {
        AlertType type = getType(chatRoomCreateRequest.chatCategory());
        Long targetId = chatRoomCreateRequest.targetId();
        String username = userRepository.getUsername(targetId);

        notificationService.send(NotificationRequest.chat(targetId, username, type, savedChatRoom.getId()));
    }

    private AlertType getType(ChatCategory chatCategory) {
        if (chatCategory == ChatCategory.MENTORING) {
            return AlertType.FEEDBACK;
        }
        if (chatCategory == ChatCategory.NETWORKING) {
            return AlertType.GATHERING;
        }

        return AlertType.COFFEE_CHAT;
    }

    @Transactional
    public void participation(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User user =  getUser(userId);
        chatUserService.saveChatUser(chatRoom, user);
    }

    @Transactional
    public void leave(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User user = getUser(userId);
        chatUserService.deleteChatUser(chatRoom, user);
    }

    public ChatRoom getChatRoom(Long chatRoomId) {
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
