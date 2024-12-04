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
        LocalDateTime sendAt = TypeConverter.StringToLocalDateTime(chatResponse.getSendAt());

        redisTemplate
                .opsForZSet()
                .add(TypeConverter.LongToString(chatResponse.getChatRoomId()), chatResponse, TypeConverter.LocalDateTimeToDouble(sendAt));

        redisTemplate
                .opsForZSet()
                .add(NEW_CHAT, chatResponse, TypeConverter.LocalDateTimeToDouble(sendAt));

        redisTemplate.expire(TypeConverter.LongToString(chatResponse.getChatRoomId()), Duration.ofDays(1));
        return chatResponse;
    }



    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
