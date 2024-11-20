package com.palettee.chat.service;

import com.palettee.chat.domain.ChatUser;
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

    private ChatUser makeChatUser(ChatRoom chatRoom, User user) {
        return ChatUser.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
    }
}
