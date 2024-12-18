package com.palettee.global.redis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.palettee.gathering.service.GatheringService.zSetKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCleanUp {

    private final RedisTemplate redisTemplate;

    @PostConstruct
    public void initRedisKey(){
        log.info("Redis GatheringKey삭제");
        redisTemplate.delete(zSetKey);
    }

}
