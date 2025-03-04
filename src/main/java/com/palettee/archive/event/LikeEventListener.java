package com.palettee.archive.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private static final String LIKE_KEY = "like:archiveId:";

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    @EventListener(value = LikeEvent.class)
    public void onLike(LikeEvent event) {
        redisTemplate.opsForValue().increment(LIKE_KEY + event.archiveId());
    }

}
