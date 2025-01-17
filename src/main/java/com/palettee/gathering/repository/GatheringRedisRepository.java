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

import java.util.Set;

import static com.palettee.global.Const.gathering_Page_Size;


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
        Set<GatheringResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_Gathering, 0, -1);
        if(!range.isEmpty()){
            Gathering gathering = gatheringService.getGathering(gatheringId);

            if(range.size() == gathering_Page_Size){
                log.info("내부 캐시 삭제");
                //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌
                redisTemplate.opsForZSet().removeRange(RedisConstKey_Gathering, 0, 0);
            }
            GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);
            redisTemplate.opsForZSet().add(RedisConstKey_Gathering, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.getCreateDateTime()));
        }

    }

    @Transactional(readOnly = true)
    public void updateGatheringInRedis(Long gatheringId){
        log.info("수정 이벤트");
        Set<String> keys = redisTemplate.keys(RedisConstKey_Gathering);

        if(!keys.isEmpty()){
            Gathering gathering = gatheringService.getGathering(gatheringId);

            GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);

            Double score = TypeConverter.LocalDateTimeToDouble(gatheringResponse.getCreateDateTime());

            Long removeCount = redisTemplate.opsForZSet().removeRangeByScore(RedisConstKey_Gathering, score, score);

            if(removeCount != 0){
                log.info("캐시 수정으로 인한 새로운 값 재캐싱");
                redisTemplate.opsForZSet().add(RedisConstKey_Gathering, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.getCreateDateTime()));
            }
        }
    }




}
