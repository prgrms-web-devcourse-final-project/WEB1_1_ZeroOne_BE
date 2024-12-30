package com.palettee.archive.event;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HitEventListener {

    private static final String SET_KEY_PREFIX = "hit:archiveId:";
    private static final String VALUE_KEY_PREFIX = "incr:hit:archiveId:";

    private final RedisTemplate<String, String> redisTemplate;

    @EventListener(value = HitEvent.class)
    public void onHit(HitEvent event) {

        String setKey = SET_KEY_PREFIX + event.archiveId();
        Long addResult = redisTemplate.opsForSet().add(setKey, event.email());

        if (addResult == null || addResult != 1L) {
            return;
        }

        redisTemplate.expire(setKey, Duration.ofHours(1));

        String valueKey = VALUE_KEY_PREFIX + event.archiveId();
        redisTemplate.opsForValue().increment(valueKey);
    }

}
