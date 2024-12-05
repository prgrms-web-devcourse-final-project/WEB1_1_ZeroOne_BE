package com.palettee.chat.service;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.repository.ChatCustomRepository;
import com.palettee.chat_room.service.ChatRoomService;
import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ChatRedisService {

    private final RedisTemplate<String, ChatResponse> redisTemplate;
    private final ChatCustomRepository chatCustomRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private ZSetOperations<String, ChatResponse> zSetOperations;
    private static final String NEW_CHAT = "NEW_CHAT";

    public ChatRedisService(
            @Qualifier("chatRedisTemplate") RedisTemplate<String, ChatResponse> redisTemplate,
            ChatCustomRepository chatCustomRepository,
            UserRepository userRepository,
            ChatRoomService chatRoomService) {
        this.redisTemplate = redisTemplate;
        this.chatCustomRepository = chatCustomRepository;
        this.userRepository = userRepository;
        this.chatRoomService = chatRoomService;
    }

    @PostConstruct
    private void init() {
        zSetOperations = redisTemplate.opsForZSet();
    }

    public ChatResponse addChat(String email, Long chatRoomId, ChatRequest chatRequest) {
        User user = getUser(email);
        chatRoomService.getChatRoom(chatRoomId);

        ChatResponse chatResponse = ChatResponse.toResponse(chatRoomId, user, chatRequest);
        LocalDateTime sendAt = chatResponse.getSendAt();

        redisTemplate
                .opsForZSet()
                .add(TypeConverter.LongToString(chatResponse.getChatRoomId()), chatResponse, TypeConverter.LocalDateTimeToDouble(sendAt));

        redisTemplate
                .opsForZSet()
                .add(NEW_CHAT, chatResponse, TypeConverter.LocalDateTimeToDouble(sendAt));

        redisTemplate.expire(TypeConverter.LongToString(chatResponse.getChatRoomId()), Duration.ofDays(1));
        return chatResponse;
    }

    public ChatCustomResponse getChats(Long chatRoomId, int size, LocalDateTime lastSendAt) {
        String chatRoomIdStr = TypeConverter.LongToString(chatRoomId);

        long offset = 0;
        LocalDateTime findSendAt = lastSendAt;

        if (lastSendAt == null) {
            log.info("Local = {}",LocalDateTime.now());
            findSendAt = LocalDateTime.now();
        } else {
            offset = 1;
        }

        Double cursor = TypeConverter.LocalDateTimeToDouble(findSendAt);
        log.info("cursor = {}", cursor);
        Set<ChatResponse> objects
                = zSetOperations.reverseRangeByScore(chatRoomIdStr, Double.NEGATIVE_INFINITY, cursor, offset, size+1);
        List<ChatResponse> results = objects.stream().collect(Collectors.toList());

        log.info("results size = {}", results.size());

        if(results.size() <= size) {
            ChatCustomResponse chatDataInDB = findOtherChatDataInDB(results, lastSendAt, chatRoomId, size - results.size());

            if(!results.isEmpty()) {
                for(ChatResponse chatResponse : chatDataInDB.getChats()) {
                    results.add(chatResponse);
                }
                return ChatCustomResponse.toResponseFromDto(results, chatDataInDB.isHasNext(), chatDataInDB.getLastSendAt());
            }

            return chatDataInDB;
        }

        LocalDateTime nextSendAt = results.get(size - 1).getSendAt();
        List<ChatResponse> chats = results.subList(0, size);
        return ChatCustomResponse.toResponseFromDto(chats, true, nextSendAt);
    }

    public ChatCustomResponse findOtherChatDataInDB(List<ChatResponse> results, LocalDateTime lastSendAt,
                                         Long chatRoomId, int size) {
        if(!results.isEmpty()) {
            lastSendAt = results.get(results.size() - 1).getSendAt();
        }
        ChatCustomResponse chatNoOffset = chatCustomRepository.findChatNoOffset(chatRoomId, size, lastSendAt);

        if (!chatNoOffset.getChats().isEmpty()) {
            cachingDBDataToRedis(chatNoOffset.getChats());
        }
        return chatNoOffset;
    }

    public void cachingDBDataToRedis(List<ChatResponse> chatsInDB) {
        for(ChatResponse chatResponse : chatsInDB) {
            LocalDateTime sendAt = chatResponse.getSendAt();
            redisTemplate
                    .opsForZSet()
                    .add(TypeConverter.LongToString(chatResponse.getChatRoomId()), chatResponse, TypeConverter.LocalDateTimeToDouble(sendAt));
        }
    }

    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
