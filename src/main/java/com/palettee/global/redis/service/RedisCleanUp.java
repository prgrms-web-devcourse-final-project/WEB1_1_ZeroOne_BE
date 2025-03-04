package com.palettee.global.redis.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.palettee.gathering.repository.GatheringRedisRepository.RedisConstKey_Gathering;
import static com.palettee.portfolio.repository.PortFolioRedisRepository.RedisConstKey_PortFolio;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCleanUp {

    private final RedisTemplate redisTemplate;

    @PostConstruct
    public void initRedisKey(){
        log.info("Redis 서버 로딩 시 해당 캐시 키 삭제");
        redisTemplate.delete(RedisConstKey_Gathering);
        redisTemplate.delete(RedisConstKey_PortFolio);
    }

}
