package com.palettee.global.scheduler;

import com.palettee.global.cache.MemoryCache;
import com.palettee.global.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisScheduled {

    private final MemoryCache memoryCache;

    private final RedisService redisService;

    /**
     * 1분 마다 조회수나 좋아요  DB 반영
     */
    @Scheduled(cron = "0 * * * * *")
    public void updateRedisToDb(){
        redisService.categoryToDb("portFolio");
        redisService.categoryToDb("gathering");

        //여기에 아카이빙이나 게더링 넣으시면 됩니다
    }

    /**
     * 한식간 마다 로컬 캐시에 있는 가중치를 꺼내어 Zset에 반영
     */
    @Scheduled(cron = "0 0 * * * *")
    public void rankingRedis(){

        //랭킹 반영전에 랭킹 키 한번 비워주기
        redisRankingZset();

        //순위 여부 확인 후 비우기
        redisCacheDelete();

        // 여기에 아카이브 넣으시면 됩니다.


        // 이미 가중치 반영했으니 Map 비우기
        memoryCache.clearCache();


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
        redisService.deleteKeyExceptionPattern("portFolio_*", null);

        redisService.rankingCategory("portFolio");
    }


    /**
     *  !!! 가중치가 있을때만(즉 조회수와 좋아요가 있을때만) Redis 캐시 한번 비우고 새로 교체작업
     */

    private void redisCacheDelete() {
        Long portFolio = redisService.zSetSize("portFolio");

        if(portFolio != null && portFolio > 0) {
            log.info("레디스 캐시 삭제");
            redisService.deleteKeyExceptionPattern("pf_*",null);
        }
    }


}
