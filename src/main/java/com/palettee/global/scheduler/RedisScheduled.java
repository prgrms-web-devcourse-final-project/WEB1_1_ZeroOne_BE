package com.palettee.global.scheduler;

import com.palettee.global.cache.MemoryCache;
import com.palettee.global.redis.service.RedisService;
import com.palettee.portfolio.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@Component
@RequiredArgsConstructor
public class RedisScheduled {

    private final PortFolioService portFolioService;
    private final RedisTemplate<String, Long> redisTemplate;

    private final MemoryCache memoryCache;

    private final RedisService redisService;

//    @Scheduled(cron = "0 * * * * *")
//    public void updateRedisToDb(){
//
//        redisService.categoryToDb("portFolio");
//
//
//    }
//
//    /**
//     * 한식간 마다 로컬 캐시에 있는 가중치를 꺼내어 zSet에 더해줌
//     */
//    @Scheduled(cron = "0 0 * * * *")
//    public void rankingRedis(){
//
//
//
//    }




}
