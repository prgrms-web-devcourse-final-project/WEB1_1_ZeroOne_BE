package com.palettee.global.redis.pub;

import com.palettee.chat.controller.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, ChatResponse> redisTemplate;

    public void publish(ChannelTopic topic, ChatResponse chatResponse) {
        log.info(chatResponse.getTimestamp().toString());
        redisTemplate.convertAndSend(topic.getTopic(), chatResponse);
    }
}
