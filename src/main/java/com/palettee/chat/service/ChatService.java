package com.palettee.chat.service;

import com.palettee.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRepository chatRepository;

    public void deleteChats(Long chatRoomId) {
        chatRepository.bulkDeleteChatsByChatRoomId(chatRoomId);
    }
}
