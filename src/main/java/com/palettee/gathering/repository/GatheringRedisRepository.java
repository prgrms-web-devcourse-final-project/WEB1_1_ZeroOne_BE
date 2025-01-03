package com.palettee.gathering.repository;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.redis.utils.TypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Slf4j
@RequiredArgsConstructor
public class GatheringRedisRepository {

    public final static String RedisConstKey_Gathering = "cache:firstPage:gatherings";

    private final RedisTemplate<String, GatheringResponse> redisTemplate;
    private final GatheringService gatheringService;

    @Transactional(readOnly = true)
    public void addGatheringInRedis(Long gatheringId) {
        log.info("저장 이벤트");
        Gathering gathering = gatheringService.getGathering(gatheringId);

        redisTemplate.opsForZSet().removeRange(RedisConstKey_Gathering, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌

        GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);
        redisTemplate.opsForZSet().add(RedisConstKey_Gathering, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime()));
    }

    @Transactional(readOnly = true)
    public void updateGatheringInRedis(Long gatheringId){
        log.info("수정 이벤트");
        Gathering gathering = gatheringService.getGathering(gatheringId);

        GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);

        Double score = TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime());

        redisTemplate.opsForZSet().removeRangeByScore(RedisConstKey_Gathering, score, score);
        redisTemplate.opsForZSet().add(RedisConstKey_Gathering, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime()));
    }




}
