package com.palettee.notification.service;

import com.palettee.notification.controller.dto.NotificationRequest;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean checkLikeNotification(NotificationRequest request) {
        String redisKey = makeRedisKey(request);
        Long result = redisTemplate.opsForSet()
                .add(makeRedisKey(request), request.contentTitle());


        redisTemplate.expire(redisKey, 5, TimeUnit.SECONDS);

        if (result == null) {
            log.info("알수 없는 문제 발생입니다.");
            return true;
        }

        return result != 1L;
    }

    private String makeRedisKey(NotificationRequest request) {
        return "notification:" + request.likeType().name() + "-userId:" + request.contentTitle() + "-contentId:"

                + request.contentId();
    }

}
