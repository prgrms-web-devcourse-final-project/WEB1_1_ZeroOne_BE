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

import static com.palettee.global.Const.portFolio_Page_Size;

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
            log.info("키가 비어있음");
            PortFolio portFolio = portFolioService.getUserPortFolio(portFolioId);
            Long preSize = redisTemplate.opsForZSet().size(RedisConstKey_PortFolio);

            Set<PortFolioResponse> range = redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1);// 전체 범위 조회
            if(range != null){
                removeMatchingPortFolioFromCache(range, portFolio);

                Long afterSize = redisTemplate.opsForZSet().size(RedisConstKey_PortFolio);

            log.info("afterSize = {}", afterSize);

            if(preSize.equals(afterSize) && range.size() == portFolio_Page_Size){
                log.info("캐시에 해당 아이디가 없어서 마지막 데이터 삭제");
                redisTemplate.opsForZSet().removeRange(RedisConstKey_PortFolio, 0, 0); //맨 마지막 요소 빼기 즉 score가 가장 낮은애를 빼줌
            }
            }

            PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
            redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.getCreateAt()));
        }


    }


    public void updatePortFolio(Long portFolioId){
        log.info("수정 이벤트");

        Set<String> keys = redisTemplate.keys(RedisConstKey_PortFolio);

        if(!keys.isEmpty()){
            PortFolio portFolio = portFolioService.getUserPortFolio(portFolioId);

            Long isRemove = removeMatchingPortFolioFromCache(redisTemplate.opsForZSet().range(RedisConstKey_PortFolio, 0, -1), portFolio);

            if(isRemove != 0){
                PortFolioResponse portFolioResponse = PortFolioResponse.toDto(portFolio);
                redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, portFolioResponse, TypeConverter.LocalDateTimeToDouble(portFolioResponse.getCreateAt()));
            }
        }
    }

    private Long removeMatchingPortFolioFromCache(Set<PortFolioResponse> range, PortFolio portFolio) {
        return range.stream()
                .filter(portFolioResponse -> portFolioResponse.getUserId().equals(portFolio.getUser().getId())) // 조건 필터링
                .findFirst()
                .map(matchingResponse -> {
                    log.info("같은 포트폴리오 Redis 캐시에 존재");
                    log.info("삭제된 Redis PortFolioId = {}", matchingResponse.getPortFolioId());
                    // ZSet에서 조건에 맞는 항목 제거
                    return redisTemplate.opsForZSet().remove(RedisConstKey_PortFolio, matchingResponse);
                })
                .orElse(0L); // 조건에 맞는 포트폴리오가 없을 경우 기본값 반환
    }

}
