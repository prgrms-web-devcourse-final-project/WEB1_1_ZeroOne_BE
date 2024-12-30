package com.palettee.portfolio.event;

import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.palettee.portfolio.service.PortFolioService.zSetPfKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortFolioEventHandler {

    private final PortFolioService portFolioService;
    private final RedisTemplate<String, PortFolioResponse> redisTemplate;

    @TransactionalEventListener
    public void addRedisPortFolio(PortFolioAddEventListener portFolioAddEventListener){
        log.info("저장 이벤트");
        PortFolio portFolio = portFolioService.getPortFolio(portFolioAddEventListener.portFolioId());
        Long preSize = redisTemplate.opsForZSet().size(zSetPfKey);

        redisTemplate.opsForZSet().range(zSetPfKey, 0, -1) // 전체 범위 조회
                .stream()
                .filter(portFolioResponse -> {
                    // `portFolioResponse`의 userId와 `portFolio`의 userId를 비교
                    return portFolioResponse.userId().equals(portFolio.getUser().getId());
                })
                .findFirst() // 조건에 맞는 첫 번째 항목만 처리 (유일한 항목이라고 가정)
                .ifPresent(matchingResponse -> {
                    log.info("같은 포트폴리오 Redis 캐시에 존재");
                    log.info("삭제된 Redis PortFolioId ={}", matchingResponse.portFolioId());
                    // 조건에 맞는 항목을 ZSet에서 제거
                    redisTemplate.opsForZSet().remove(zSetPfKey, matchingResponse);
                });

        Long afterSize = redisTemplate.opsForZSet().size(zSetPfKey);

        log.info("afterSize = {}", afterSize);

        if(preSize.equals(afterSize)){
            log.info("캐시에 해당 아이디가 없어서 마지막 데이터 삭제");
            redisTemplate.opsForZSet().removeRange(zSetPfKey, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌
        }

        PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
        redisTemplate.opsForZSet().add(zSetPfKey, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.createAt()));
    }

    @TransactionalEventListener
    public void updatePortFolio(PortFolioUpdateEventListener portFolioUpdateEventListener){
        log.info("수정 이벤트");
        PortFolio portFolio = portFolioService.getPortFolio(portFolioUpdateEventListener.portFolioId());

        redisTemplate.opsForZSet().range(zSetPfKey, 0, -1) // 전체 범위 조회
                .stream()
                .filter(portFolioResponse -> {
                    // `portFolioResponse`의 userId와 `portFolio`의 userId를 비교
                    return portFolioResponse.userId().equals(portFolio.getUser().getId());
                })
                .findFirst() // 조건에 맞는 첫 번째 항목만 처리 (유일한 항목이라고 가정)
                .ifPresent(matchingResponse -> {
                    log.info("같은 포트폴리오 Redis 캐시에 존재");
                    log.info("삭제된 Redis PortFolioId ={}", matchingResponse.portFolioId());
                    // 조건에 맞는 항목을 ZSet에서 제거
                    redisTemplate.opsForZSet().remove(zSetPfKey, matchingResponse);
                });

        PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
        redisTemplate.opsForZSet().add(zSetPfKey, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.createAt()));

    }


}
