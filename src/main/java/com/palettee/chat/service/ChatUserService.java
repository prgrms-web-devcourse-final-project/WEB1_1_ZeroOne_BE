package com.palettee.chat.service;

import com.palettee.chat.domain.ChatUser;
import com.palettee.chat.exception.ChatUserNotFoundException;
import com.palettee.chat.repository.ChatUserRepository;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUserService {
    private final ChatUserRepository chatUserRepository;

    public void saveChatUser(ChatRoom chatRoom, User user) {
        ChatUser chatUser = makeChatUser(chatRoom, user);
        chatUserRepository.save(chatUser);
    }

    public void deleteChatUser(ChatRoom chatRoom, User user) {
        ChatUser chatUser = getChatUser(chatRoom, user);
        chatUserRepository.delete(chatUser);
    }

    public boolean isExist(ChatRoom chatRoom, User user) {
        return chatUserRepository.existsByChatRoomAndUser(chatRoom, user);
    }

    private ChatUser makeChatUser(ChatRoom chatRoom, User user) {
        return ChatUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
    }

    private ChatUser getChatUser(ChatRoom chatRoom, User user) {
        return chatUserRepository
                .findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> ChatUserNotFoundException.EXCEPTION);
    }
}
