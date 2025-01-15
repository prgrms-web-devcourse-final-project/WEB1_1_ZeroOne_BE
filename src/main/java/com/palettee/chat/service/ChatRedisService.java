package com.palettee.chat.service;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatCustomResponse;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.repository.ChatRepository;
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

import static com.palettee.global.Const.*;

@Repository
@Slf4j
public class ChatRedisService {

    private final RedisTemplate<String, ChatResponse> redisTemplate;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private ZSetOperations<String, ChatResponse> zSetOperations;

    public ChatRedisService(
            @Qualifier("chatRedisTemplate") RedisTemplate<String, ChatResponse> redisTemplate,
            ChatRepository chatRepository,
            UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
        zSetOperations = redisTemplate.opsForZSet();
    }

    public ChatResponse addChat(String email, Long chatRoomId, ChatRequest chatRequest) {
        User user = getUser(email);

        ChatResponse chatResponse = ChatResponse.toResponse(chatRoomId, user, chatRequest);
        String key = CHATROOM_KEY_PREFIX + TypeConverter.LongToString(chatResponse.getChatRoomId());
        double score = TypeConverter.LocalDateTimeToDouble(chatResponse.getSendAt());

        redisTemplate
                .opsForZSet()
                .add(key, chatResponse, score);

        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > CHAT_MAX_SIZE) {
            redisTemplate.opsForZSet().removeRange(key, 0, 0);
        }

        redisTemplate
                .opsForZSet()
                .add(NEW_CHAT, chatResponse, score);

        redisTemplate.expire(key, Duration.ofDays(1));
        return chatResponse;
    }

    public ChatCustomResponse getChats(Long chatRoomId, int size, LocalDateTime lastSendAt) {
        String key = CHATROOM_KEY_PREFIX + TypeConverter.LongToString(chatRoomId);
        long offset = 0;
        LocalDateTime findSendAt = lastSendAt;

        if (lastSendAt == null) {
            findSendAt = LocalDateTime.now();
        } else {
            offset = 1;
        }

        Double cursor = TypeConverter.LocalDateTimeToDouble(findSendAt);
        Set<ChatResponse> objects
                = zSetOperations.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, cursor, offset, size+1);
        List<ChatResponse> results = objects.stream().collect(Collectors.toList());

        redisTemplate.expire(key, Duration.ofDays(1));

        // size와 작거나 같으면 DB에서 조회
        if(results.size() <= size) {
            // 부족한 데이터 만큼 DB에서 조회
            ChatCustomResponse chatDataInDB = findOtherChatDataInDB(results, lastSendAt, chatRoomId, size - results.size());

            // DB에서 조회한 데이터가 존재하면 Redis에 데이터를 넣는다.
            if (!chatDataInDB.getChats().isEmpty()) {
                Long redisTotalSize = redisTemplate.opsForZSet().size(key);
                cachingDBDataToRedis(redisTotalSize, chatDataInDB.getChats());
            }

            // DB에서 조회한 데이터 list를 Redis에서 조회한 데이터 list와 합친다.
            results.addAll(chatDataInDB.getChats());
            return ChatCustomResponse.toResponseFromDto(results, chatDataInDB.isHasNext());
        }

        return ChatCustomResponse.toResponseFromDto(results.subList(0, size), true);
    }

    public ChatCustomResponse findOtherChatDataInDB(List<ChatResponse> results, LocalDateTime lastSendAt,
                                         Long chatRoomId, int size) {
        if(!results.isEmpty()) {
            lastSendAt = results.get(results.size() - 1).getSendAt();
        }
        return chatRepository.findChatNoOffset(chatRoomId, size, lastSendAt);
    }

    // 허용 가능한 만큼만 Redis에 넣는다.
    public void cachingDBDataToRedis(Long redisTotalSize, List<ChatResponse> chatsInDB) {
        long possibleSize = CHAT_MAX_SIZE - redisTotalSize;
        for(int i = 0; i < possibleSize; i++) {
            ChatResponse chatResponse = chatsInDB.get(i);
            redisTemplate
                    .opsForZSet()
                    .add(TypeConverter.LongToString(chatResponse.getChatRoomId()),
                            chatResponse, TypeConverter.LocalDateTimeToDouble(chatResponse.getSendAt()));
            if(i == chatsInDB.size() - 1) {
                break;
            }
        }
    }

    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
