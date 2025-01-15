package com.palettee.chat.service;

import com.palettee.chat.domain.ChatUser;
import com.palettee.chat.exception.ChatUserNotFoundException;
import com.palettee.chat.repository.ChatUserRepository;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUserService {
    private final ChatUserRepository chatUserRepository;

    public void saveChatUser(ChatRoom chatRoom, User user, boolean isDeleted) {
        ChatUser chatUser = makeChatUser(chatRoom, user, isDeleted);
        chatUserRepository.save(chatUser);
    }

    public void deleteChatUsers(Long chatRoomId) {
        chatUserRepository.deleteAllByChatRoomId(chatRoomId);
    }

    public int countChatRoom(Long chatRoomId) {
        return chatUserRepository.countChatUsersByChatRoom(chatRoomId, true);
    }

    public ChatUser getChatUser(Long chatRoomId, Long userId, boolean isDeleted) {
        return chatUserRepository
                .findByChatRoomAndUser(chatRoomId, userId, isDeleted)
                .orElseThrow(() -> ChatUserNotFoundException.EXCEPTION);
    }

    public List<ChatUser> getMyChatUsers(User user) {
        return chatUserRepository.getChatUsersByMe(user);
    }

    private ChatUser makeChatUser(ChatRoom chatRoom, User user, boolean isDeleted) {
        return ChatUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .isDeleted(isDeleted)
                .build();
    }
}
