package com.palettee.portfolio.repository;

import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PortFolioRedisRepository
{

    public final static String RedisConstKey_PortFolio = "cache:firstPage:portFolios";

    private final PortFolioService portFolioService;
    private final RedisTemplate<String, PortFolioResponse> redisTemplate;


    public void addPortFolioInRedis(Long portFolioId){

        log.info("저장 이벤트");
        Set<String> keys = redisTemplate.keys(RedisConstKey_PortFolio);

        if(!keys.isEmpty()){
            PortFolio portFolio = portFolioService.getUserPortFolio(portFolioId);
            Long preSize = redisTemplate.opsForZSet().size(RedisConstKey_PortFolio);

            redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1) // 전체 범위 조회
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
                        redisTemplate.opsForZSet().remove(RedisConstKey_PortFolio, matchingResponse);
                    });

            Long afterSize = redisTemplate.opsForZSet().size(RedisConstKey_PortFolio);

            log.info("afterSize = {}", afterSize);

            if(preSize.equals(afterSize)){
                log.info("캐시에 해당 아이디가 없어서 마지막 데이터 삭제");
                redisTemplate.opsForZSet().removeRange(RedisConstKey_PortFolio, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌
            }

            PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
            redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.createAt()));
        }


    }


    public void updatePortFolio(Long portFolioId){
        log.info("수정 이벤트");

        Set<String> keys = redisTemplate.keys(RedisConstKey_PortFolio);

        if(!keys.isEmpty()){
            PortFolio portFolio = portFolioService.getUserPortFolio(portFolioId);

            redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1) // 전체 범위 조회
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
                        redisTemplate.opsForZSet().remove(RedisConstKey_PortFolio, matchingResponse);
                    });

            PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
            redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.createAt()));
        }


    }

}
