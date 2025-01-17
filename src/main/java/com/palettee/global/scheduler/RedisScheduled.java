package com.palettee.global.scheduler;

import com.palettee.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisScheduled {

    private final RedisTemplate<String, Long> redisTemplate;

    private final RedisService redisService;

    /**
     * 1분 마다 조회수나 좋아요  DB 반영
     */
    @Scheduled(cron = "0 * * * * *")
    public void updateRedisToDb(){
        redisService.categoryToDb("portFolio");
        redisService.categoryToDb("gathering");

    }

    /**
     * 매 1시간 마다 가중치 반영
     */
    @Scheduled(cron = "0 0 * * * *")
    public void rankingRedis(){

        //랭킹 반영전에 랭킹 키 한번 비워주기
        redisRankingZset();




        //카운트 redis 한번 비우기
        redisService.deleteKeyExceptionPattern("View_*", "_user");
        redisService.deleteKeyExceptionPattern("Like_*", "_user");
    }

    /**
     * 자정 시간 조회수 리셋 즉 하루에 한번은 카테고리를 조회 할 수 있음
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void hitsSet(){
        redisService.deleteKeyIncludePattern("View_*", "_user");
    }

    /**
     * ZSET 한번 비우고 새로운 가중치 ZSET 반영
     */
    private void redisRankingZset() {
        redisService.rankingCategory("portFolio");
        redisService.rankingCategory("gathering");
    }



}
