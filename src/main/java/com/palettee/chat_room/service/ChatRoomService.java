package com.palettee.chat_room.service;

import com.palettee.chat.domain.ChatUser;
import com.palettee.chat.service.ChatImageService;
import com.palettee.chat.service.ChatService;
import com.palettee.chat.service.ChatUserService;
import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomListResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserService chatUserService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final ChatImageService chatImageService;

    @Transactional
    public ChatRoomResponse saveChatRoom(ChatRoomCreateRequest chatRoomCreateRequest,
                                         User user) {
        ChatRoom chatRoom = chatRoomCreateRequest.toEntityChatRoom();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        User findUser = getUser(user.getId());
        User targetUser = getUser(chatRoomCreateRequest.targetId());
        chatUserService.saveChatUser(savedChatRoom, findUser, true);
        chatUserService.saveChatUser(savedChatRoom, targetUser, false);

        sendNotification(chatRoomCreateRequest, savedChatRoom, user.getId());

        return ChatRoomResponse.of(savedChatRoom);
    }

    private void sendNotification(ChatRoomCreateRequest chatRoomCreateRequest, ChatRoom savedChatRoom, Long userId) {
        AlertType type = getType(chatRoomCreateRequest.chatCategory());
        Long targetId = chatRoomCreateRequest.targetId();
        String username = userRepository.getUsername(targetId);

        notificationService.send(NotificationRequest.chat(targetId, username, type, savedChatRoom.getId(), userId));
    }

    private AlertType getType(ChatCategory chatCategory) {
        if (chatCategory == ChatCategory.MENTORING) {
            return AlertType.FEEDBACK;
        }
        if (chatCategory == ChatCategory.GATHERING) {
            return AlertType.GATHERING;
        }

        return AlertType.COFFEE_CHAT;
    }

    @Transactional
    public void participation(Long chatRoomId, User user) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        User findUser = getUser(user.getId());

        ChatUser chatUser = chatUserService.getChatUser(chatRoomId, findUser.getId(), false);

        chatUser.participation();
    }

    @Transactional
    public void leave(Long chatRoomId, User user) {
        if(chatUserService.countChatRoom(chatRoomId) > 1) {
            ChatUser chatUser = chatUserService.getChatUser(chatRoomId, user.getId(), true);
            chatUser.leave();
        } else {
            deleteAll(chatRoomId);
        }
    }

    private void deleteAll(Long chatRoomId) {
        chatUserService.deleteChatUsers(chatRoomId);
        chatImageService.deleteChatImages(chatRoomId);
        chatService.deleteChats(chatRoomId);
        chatRoomRepository.deleteByChatRoomId(chatRoomId);
    }

    public ChatRoomListResponse getMyChatRooms(User user) {
        List<ChatUser> chatUsers = chatUserService.getMyChatUsers(user);
        return ChatRoomListResponse.toResponse(chatUsers);
    }

    public ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository
                .findById(chatRoomId)
                .orElseThrow(() -> ChatRoomNotFoundException.EXCEPTION);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).get();
    }
}
