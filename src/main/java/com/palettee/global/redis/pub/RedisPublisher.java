package com.palettee.global.redis.pub;

import com.palettee.chat.controller.dto.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, ChatResponse> redisTemplate;

    public RedisPublisher(@Qualifier("chatPubTemplate") RedisTemplate<String, ChatResponse> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(ChannelTopic topic, ChatResponse chatResponse) {
        redisTemplate.convertAndSend(topic.getTopic(), chatResponse);
    }
}
