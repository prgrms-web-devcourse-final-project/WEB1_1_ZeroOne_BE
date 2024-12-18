package com.palettee.gathering.event;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.Gathering;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.redis.utils.TypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.palettee.gathering.service.GatheringService.zSetKey;


@Component
@RequiredArgsConstructor
public class GatheringEventHandler {

    private final GatheringService gatheringService;
    private final RedisTemplate<String, GatheringResponse> redisTemplate;


    @TransactionalEventListener
    public void addRedisGathering(GatheringEventListener gatheringEventListener){
        Gathering gathering = gatheringService.getGathering(gatheringEventListener.gatheringId());

        redisTemplate.opsForZSet().removeRange(zSetKey, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌

        GatheringResponse gatheringResponse = GatheringResponse.toDto(gathering);
        redisTemplate.opsForZSet().add(zSetKey, gatheringResponse, TypeConverter.LocalDateTimeToDouble(gatheringResponse.createDateTime()));
    }
}
