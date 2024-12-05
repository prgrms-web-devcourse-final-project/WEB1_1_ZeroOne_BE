package com.palettee.chat.service;

import com.palettee.chat.controller.dto.response.ChatImgUrl;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.repository.ChatJdbcRepository;
import com.palettee.chat.service.dto.ChatImageSaveDto;
import com.palettee.chat.service.dto.ChatSaveDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ChatWriteBackScheduling {
    private final RedisTemplate<String, ChatResponse> redisTemplate;
    private final ChatJdbcRepository chatJdbcRepository;

    public ChatWriteBackScheduling(@Qualifier("chatRedisTemplate") RedisTemplate<String, ChatResponse> redisTemplate,
                                   ChatJdbcRepository chatJdbcRepository) {
        this.redisTemplate = redisTemplate;
        this.chatJdbcRepository = chatJdbcRepository;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void writeBack() {
        BoundZSetOperations<String, ChatResponse> setOperation = redisTemplate.boundZSetOps("NEW_CHAT");

        ScanOptions scanOptions = ScanOptions.scanOptions().build();

        List<ChatSaveDto> chatList = new ArrayList<>();
        List<ChatImageSaveDto> chatImageList = new ArrayList<>();

        Cursor<ZSetOperations.TypedTuple<ChatResponse>> cursor = setOperation.scan(scanOptions);
        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<ChatResponse> tupleChatResponse = cursor.next();
            ChatResponse chatResponse = tupleChatResponse.getValue();

            String chatId = UUID.randomUUID().toString();

            ChatSaveDto chatSaveDto = ChatSaveDto.toDto(chatId, chatResponse);
            chatList.add(chatSaveDto);

            if(chatResponse.getImgUrls() != null && !chatResponse.getImgUrls().isEmpty()) {
                for(ChatImgUrl chatImgUrl : chatResponse.getImgUrls()) {
                    chatImageList.add(ChatImageSaveDto.toDto(chatId, chatImgUrl.getImgUrl()));
                }
            }
        }

        if(!chatList.isEmpty() || !chatImageList.isEmpty()) {
            chatJdbcRepository.batchInsertChats(chatList, chatImageList);
            redisTemplate.delete("NEW_CHAT");
        }
    }
}
