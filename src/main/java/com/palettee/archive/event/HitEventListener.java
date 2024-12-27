package com.palettee.archive.event;

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
    private static final String ZERO = "0";

    private final RedisTemplate<String, String> redisTemplate;

    @EventListener(value = HitEvent.class)
    public void onHit(HitEvent event) {

        Long add = redisTemplate.opsForSet().add(SET_KEY_PREFIX + event.archiveId() + event.email(), event.email());
        if (add == null || add != 1L) {
            return;
        }

        String currentHits = redisTemplate.opsForValue().get(VALUE_KEY_PREFIX + event.archiveId());
        if (currentHits == null) {
            redisTemplate.opsForValue().set(VALUE_KEY_PREFIX + event.archiveId(), ZERO);
        }

        redisTemplate.opsForValue().increment(VALUE_KEY_PREFIX + event.archiveId());
    }

}
