package com.palettee.chat.service;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat.repository.ChatRepository;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.service.ChatRoomService;
import com.palettee.global.handler.exception.ChatContentNullException;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatImageService chatImageService;

    @Transactional
    public ChatResponse saveChat(String email, Long chatRoomId, ChatRequest chatRequest) {
        User user = getUser(email);
        ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);

        Chat chat = chatRequest.toEntityChat(user, chatRoom);
        Chat savedChat = chatRepository.save(chat);

        List<ChatImage> savedChatImages = null;
        if (chatRequest.imgUrls() != null && !chatRequest.imgUrls().isEmpty()) {
            List<ChatImage> chatImages = chatRequest.toEntityChatImages(savedChat);
            savedChatImages = chatImageService.saveChatImages(chatImages);
        }

        if (chatRequest.content() == null && savedChatImages == null) {
            throw ChatContentNullException.EXCEPTION;
        }

        return ChatResponse.toResponse(chatRoomId, savedChat, savedChatImages);
    }

    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
