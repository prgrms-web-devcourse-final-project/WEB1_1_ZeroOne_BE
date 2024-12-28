package com.palettee.gathering.service.schedule;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.service.GatheringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.palettee.gathering.service.GatheringService.zSetKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckScheduledTasks {

    private final GatheringService gatheringService;

    private final RedisTemplate<String, GatheringResponse> redisTemplate;

    /**
     * 상태값 변경시 자정에 키 삭제 -> 정합성을 맞추기 위해
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkScheduledTasks() {
        log.info("만료 스케줄러 실행");
        gatheringService.updateGatheringStatus();
        deleteCacheRedis();
    }

    private void deleteCacheRedis(){
        redisTemplate.delete(zSetKey);
    }






}
