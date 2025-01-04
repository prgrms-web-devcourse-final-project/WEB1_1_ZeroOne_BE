package com.palettee.global.cache;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.palettee.global.Const.VIEW_PREFIX;

@Service
@RequiredArgsConstructor
public class RedisWeightCache {

    private static final Logger log = LoggerFactory.getLogger(RedisWeightCache.class);
    private final RedisTemplate<String, Long> redisTemplate;


    public void put(String key, Long value) {
        // 로컬 캐시안에 가중치가 이미 있었으면
            long adjustedValue = key.startsWith(VIEW_PREFIX) ? value : value * 5; // View 는 가중치 1점 like는 가중치 5점

            String weightKey = key + ":weight";

            log.info("adding weight to redis: {}", weightKey);

            redisTemplate.opsForValue().increment(weightKey, adjustedValue);
    }

    /**
     *
     * @param
     * @return
     * View 해당 하는 가중치가 담긴 Map을 가져옴
     */
    public Map<Long, Long> getCache(String keyPattern) {

        Set<String> redisKeys = redisTemplate.keys("*:weight");

        log.info("redisKeys: {}", redisKeys.size());


        redisKeys = redisKeys.stream()
                .filter(redisKey -> redisKey.contains(keyPattern))
                .collect(Collectors.toSet());

        log.info("get cache: {}", redisKeys.size());

       return   redisKeys
                .stream()
                .collect(Collectors.toMap(
                        key ->Long.parseLong(key.split(":")[1]),
                        key ->redisTemplate.opsForValue().get(key)
                ));

    }


    public  boolean getSize(){
        Set<String> keys = redisTemplate.keys("*:weight");

        if(keys != null && !keys.isEmpty()){
            return true;
        }
        return false;
    }




}
