package com.palettee.archive.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private static final String LIKE_KEY = "like:archiveId:";

    private final RedisTemplate<String, String> redisTemplate;

    @EventListener(value = LikeEvent.class)
    public void onLike(LikeEvent event) {
        redisTemplate.opsForValue().setIfAbsent(LIKE_KEY + event.archiveId(), String.valueOf(event.userId()));
    }

}
