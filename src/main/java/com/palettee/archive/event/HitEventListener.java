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
    private static final Duration EXPIRE_DURATION = Duration.ofDays(1);

    private final RedisTemplate<String, String> redisTemplate;

    @Async
    @EventListener(value = HitEvent.class)
    public void handleHitEvent(HitEvent event) {
        if (isUniqueHit(event)) {
            incrementHitCount(event.archiveId());
        }
    }

    private boolean isUniqueHit(HitEvent event) {
        String setKey = generateSetKey(event.archiveId());
        Long addResult = redisTemplate.opsForSet().add(setKey, event.email());

        if (addResult != null && addResult == 1L) {
            redisTemplate.expire(setKey, EXPIRE_DURATION);
            return true;
        }
        return false;
    }

    private void incrementHitCount(Long archiveId) {
        String valueKey = generateValueKey(archiveId);
        redisTemplate.opsForValue().increment(valueKey);
    }

    private String generateSetKey(Long archiveId) {
        return SET_KEY_PREFIX + archiveId;
    }

    private String generateValueKey(Long archiveId) {
        return VALUE_KEY_PREFIX + archiveId;
    }

}
