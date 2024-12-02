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

    @Scheduled(cron = "0 * * * * *")
    public void updateRedisToDb(){
        redisService.categoryToDb("portFolio");

        //여기에 아카이빙이나 게더링 넣으시면 됩니다
    }

    /**
     * 한식간 마다 로컬 캐시에 있는 가중치를 꺼내어 zSet에 더해줌
     */
    @Scheduled(cron = "0 0 * * * *")
    public void rankingRedis(){

        //랭킹 반영전에 랭킹 키 한번 비워주기
        redisRankingZset();

        //순위 여부 확인 후 비우기
        redisCacheDelete();

        // 여기에 아카이브 넣으시면 됩니다.


        // 메모리 캐시 비워주기
        memoryCache.clearCache();


        //redis 캐시 View vo
        redisService.deleteKeyExceptionPattern("View_*", "_user");
        redisService.deleteKeyExceptionPattern("Like_*", "_user");
    }

    private void redisRankingZset() {
        redisService.deleteKeyExceptionPattern("portFolio_*", null);

        redisService.rankingCategory("portFolio");
    }

    /**
     * 자정이 지나면 조회수 다시 리셋
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void hitsSet(){
        redisService.deleteKeyIncludePattern("View_*", "_user");
    }

    private void redisCacheDelete() {
        Long portFolio = redisService.zSetSize("portFolio");

        if(portFolio != null && portFolio > 0) {
            log.info("레디스 캐시 삭제");
            redisService.deleteKeyExceptionPattern("pf_*",null);
        }
    }


}
