package com.palettee.chat.service;

import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat.repository.ChatJdbcRepository;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.service.ChatRoomService;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ChatWriteBackScheduling {
    private final RedisTemplate<String, ChatResponse> redisTemplate;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatJdbcRepository chatJdbcRepository;

    public ChatWriteBackScheduling(@Qualifier("chatRedisTemplate") RedisTemplate<String, ChatResponse> redisTemplate,
                                   UserRepository userRepository,
                                   ChatRoomService chatRoomService,
                                   ChatJdbcRepository chatJdbcRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.chatRoomService = chatRoomService;
        this.chatJdbcRepository = chatJdbcRepository;
    }

    // Todo user와 chatRoom 조회는 성능이 떨어진다. 그니깐 중간에 DTO를 하나 두어서 필요한 정보만 넣고 batchInsert를 하자.
    // Todo 하지만 연관관계 매핑에서 문제가 발생함 이 부분 질문 사항
    @Scheduled(cron = "0 0/1 * * * *")
    public void writeBack() {
        BoundZSetOperations<String, ChatResponse> setOperation = redisTemplate.boundZSetOps("NEW_CHAT");

        ScanOptions scanOptions = ScanOptions.scanOptions().build();

        List<Chat> chatList = new ArrayList<>();
        List<ChatImage> chatImageList = new ArrayList<>();

        Cursor<ZSetOperations.TypedTuple<ChatResponse>> cursor = setOperation.scan(scanOptions);
        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<ChatResponse> tupleChatResponse = cursor.next();
            ChatResponse chatResponse = tupleChatResponse.getValue();

            User user = getUser(chatResponse.getEmail());
            ChatRoom chatRoom = chatRoomService.getChatRoom(chatResponse.getChatRoomId());

            Chat chatEntity = chatResponse.toChatEntity(user, chatRoom);
            chatList.add(chatEntity);

            if(chatResponse.getImgUrls() != null || !chatResponse.getImgUrls().isEmpty()) {
                for(ChatImage chatImage : chatResponse.toChatImageEntities(chatEntity)) {
                    chatImageList.add(chatImage);
                }
            }
        }

        if(!chatList.isEmpty() || !chatImageList.isEmpty()) {
            chatJdbcRepository.batchInsertChats(chatList, chatImageList);
            redisTemplate.delete("NEW_CHAT");
        }
    }

    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
