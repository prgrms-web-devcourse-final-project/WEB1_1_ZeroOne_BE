package com.palettee.gathering.event;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.redis.utils.TypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

import static com.palettee.gathering.service.GatheringService.zSetKey;


@Component
@RequiredArgsConstructor
@Slf4j
public class GatheringEventHandler {

    private final GatheringService gatheringService;
    private final RedisTemplate<String, GatheringResponse> redisTemplate;


    @TransactionalEventListener
    public void addRedisGathering(GatheringAddEventListener gatheringEventListener){
        log.info("저장 이벤트");
        Gathering gathering = gatheringService.getGathering(gatheringEventListener.gatheringId());

        redisTemplate.opsForZSet().removeRange(zSetKey, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌

        GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);
        redisTemplate.opsForZSet().add(zSetKey, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime()));
    }


    @TransactionalEventListener
    public void updateRedisGathering(GatheringPutEventListener gatheringEventListener){
        log.info("수정 이벤트");
        Gathering gathering = gatheringService.getGathering(gatheringEventListener.gatheringId());

        GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);

        Double score = TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime());

        redisTemplate.opsForZSet().removeRangeByScore(zSetKey, score, score);
        redisTemplate.opsForZSet().add(zSetKey, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime()));
    }

    @TransactionalEventListener
    public void deleteRedisGathering(GatheringDeleteEventListener gatheringDeleteEventListener){
        log.info("삭제 이벤트");
        Double score = TypeConverter.LocalDateTimeToDouble(gatheringDeleteEventListener.gathering().getCreateAt());
        Set<GatheringResponse> gatheringResponses = redisTemplate.opsForZSet().rangeByScore(zSetKey, score, score);

        log.info("gatheringResponses.size: {}", gatheringResponses.size());

        if(!gatheringResponses.isEmpty() && gatheringResponses.size() != 0){
            log.info("Redis 내부에서 삭제");
            redisTemplate.delete(zSetKey);
        }
    }

}
