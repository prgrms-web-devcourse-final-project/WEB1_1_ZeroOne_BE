package com.palettee.chat_room.service;


import com.palettee.chat_room.controller.dto.request.ChatRoomCreateRequest;
import com.palettee.chat_room.controller.dto.response.ChatRoomResponse;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoomResponse saveChatRoom(ChatRoomCreateRequest chatRoomCreateRequest) {
        ChatRoom chatRoom = chatRoomCreateRequest.toEntityChatRoom();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomResponse.of(savedChatRoom);
    }
}
