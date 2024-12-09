package com.palettee.chat_room.service;

import com.palettee.chat.service.ChatUserService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.domain.ChatCategory;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.exception.ChatRoomNotFoundException;
import com.palettee.chat_room.exception.DuplicateParticipationException;
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
    public ChatRoomResponse saveChatRoom(ChatRoomCreateRequest chatRoomCreateRequest,
                                         User user) {
        ChatRoom chatRoom = chatRoomCreateRequest.toEntityChatRoom();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        User findUser = getUser(user.getId());
        chatUserService.saveChatUser(savedChatRoom, findUser);

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
    public void participation(Long chatRoomId, User user) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User findUser = getUser(user.getId());

        validDuplicateParticipation(chatRoom, findUser);

        chatUserService.saveChatUser(chatRoom, findUser);
    }

    @Transactional
    public void leave(Long chatRoomId, User user) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User findUser = getUser(user.getId());

        if(chatUserService.isExist(chatRoom, user)) {
            chatUserService.deleteChatUser(chatRoom, findUser);
        }
    }

    public ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> ChatRoomNotFoundException.EXCEPTION);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).get();
    }

    private void validDuplicateParticipation(ChatRoom chatRoom, User user) {
        if(chatUserService.isExist(chatRoom, user)) {
            throw DuplicateParticipationException.EXCEPTION;
        }
    }
}
