package com.palettee.global.scheduler;

import com.palettee.global.cache.MemoryCache;
import com.palettee.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisScheduled {

    private final MemoryCache memoryCache;

    private final RedisService redisService;

    @Scheduled(cron = "0 * * * * *")
    public void updateRedisToDb(){
        redisService.categoryToDb("portFolio");

        //여기에 아카이빙이나 게더링 넣으시면 됩니다
    }

    /**
     * 한식간 마다 로컬 캐시에 있는 가중치를 꺼내어 zSet에 더해줌
     */
    @Scheduled(cron = "3 * * * * *")
    public void rankingRedis(){

        //랭킹 반영전에 랭킹 키 한번 비워주기
        redisService.deleteKeyPatten("portFolio_*", null);


        redisService.rankingCategory("portFolio");

        // 여기에 아카이브 넣으시면 됩니다.

        // 메모리 캐시 비워주기
        memoryCache.clearCache();

        Map<String, Long> localCache = memoryCache.getLocalCache();

        System.out.println("localCache: " + localCache.size());

        //redis 캐시 View vo
        redisService.deleteKeyPatten("View_*", null);
        redisService.deleteKeyPatten("Like_*", "_user");

    }




}
