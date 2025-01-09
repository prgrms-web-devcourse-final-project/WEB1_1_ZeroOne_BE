package com.palettee.archive.event;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HitEventListener {

    private static final String SET_KEY_PREFIX = "hit:archiveId:";
    private static final String VALUE_KEY_PREFIX = "incr:hit:archiveId:";

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    @EventListener(value = HitEvent.class)
    public void onHit(HitEvent event) {

        String setKey = SET_KEY_PREFIX + event.archiveId();
        Long addResult = redisTemplate.opsForSet().add(setKey, event.email());

        if (addResult == null || addResult != 1L) {
            return;
        }

        redisTemplate.expire(setKey, Duration.ofDays(1));

        String valueKey = VALUE_KEY_PREFIX + event.archiveId();
        redisTemplate.opsForValue().increment(valueKey);
    }

}
