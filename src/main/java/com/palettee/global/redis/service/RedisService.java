package com.palettee.global.redis.service;

import com.palettee.global.cache.MemoryCache;
import com.palettee.likes.controller.dto.LikeDto;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.service.LikeService;
import com.palettee.portfolio.service.PortFolioRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Long> redisTemplate;
    private final PortFolioRedisService portFolioRedisService;
    private final MemoryCache memoryCache;
    private final LikeService likeService;

    /**
     * redis에 해당 뷰 카운팅
     * @param targetId 는 achieveId, portFolioId
     * @param category 는 achieve, portFolio
     */
    public void viewCount(Long targetId, Long userId ,String category) {
        String key = VIEW_PREFIX + category + ": " + targetId;
        String userKey = key + "_user";

        // 해당 카테고리의 아이디에 대한 유저가 존재하는지 여부  -> 존재하면 해당 목록에 저장 후 카운팅, 존재 x 노 카운팅
        Long validation = redisTemplate.opsForSet().add(userKey, userId);

        if(validation != null && validation > 0) {
            log.info("조회수 카운팅");
            redisTemplate.opsForValue().increment(key, 1L);
        }else{
            log.info("24시간이 지나야 조회 할 수 있습니다");
        }

    }

    /**
     * 좋아요 카운트 처리
     * @param targetId 는 achieveId, portFolioId, gatheringId
     * @param userId 유저 아이디
     * @param category achieve, portFolio, gathering
     */
    public boolean likeCount(Long targetId, Long userId, String category) {
        String key = LIKE_PREFIX + category + ": " + targetId;
        String userKey = key + "_user";

        Long validation = redisTemplate.opsForSet().add(userKey, userId);

        if (validation != null && validation > 0) {
            log.info("좋아요를 눌렀습니다");
            redisTemplate.opsForValue().increment(key, 1L);
            return true;
        } else {
            log.info("좋아요를 취소하였습니다");
            redisTemplate.opsForSet().remove(userKey, userId);
            redisTemplate.opsForValue().set(key, 0L);
            return false;
        }
    }

    /**
     * 스케줄러를 돌려서 1분마다 redis -> db로 반영
     */
    public void categoryToDb(String category) {
        String viewKeys = VIEW_PREFIX + category + ": ";
        String likeKeys = LIKE_PREFIX + category + ": ";

        viewRedisToDB(viewKeys);
        likeRedisToDB(likeKeys, category);
    }

    /**
     * redis의 조회수 데이터를 DB에 반영
     */
    public void viewRedisToDB(String viewKeys) {
        Set<String> redisKeys = redisTemplate.keys(viewKeys + "*");

        if (redisKeys.isEmpty()) {
            log.info("해당 키에 대한 값이 없음");
            return;
        }

        redisKeys = redisKeys.stream()
                .filter(redisKey -> !redisKey.contains("_user"))
                .collect(Collectors.toSet());

        redisKeys.forEach(redisKey -> {
            log.info("카운트");
            long targetId = Long.parseLong(redisKey.replace(viewKeys, "").trim());
            Long countViews = Optional.ofNullable(redisTemplate.opsForValue().get(redisKey)).orElse(0L);
            log.info("targetId: {}, countViews: {}", targetId, countViews);

            if (countViews > 0) {
                updateViewDB(redisKey, countViews, targetId, viewKeys);
            }
        });
    }

    /**
     * redis의 좋아요 데이터를 DB에 반영
     */
    public void likeRedisToDB(String likeKeys, String category) {
        Set<String> redisKeys = redisTemplate.keys(likeKeys + "*");

        if (redisKeys.isEmpty()) {
            return;
        }

        redisKeys = redisKeys.stream()
                .filter(redisKey -> !redisKey.contains("_user"))
                .collect(Collectors.toSet());

        redisKeys.forEach(redisKey -> {
            log.info("redisKey ={}", redisKey);
            long targetId = Long.parseLong(redisKey.replace(likeKeys, "").trim());
            insertLikeDB(category, redisKey, targetId, likeKeys);
        });
    }

    /**
     * 카테고리의 순위를 Redis에서 계산하여 zset에 반영
     */
    public void rankingCategory(String category) {
        String zSetKey = category + "_Ranking";
        redisTemplate.opsForZSet().removeRange(zSetKey, 0, -1); // 인기 순위 목록 한번 비우기

        Map<String, Long> viewCache = memoryCache.getViewCache(category);
        Map<String, Long> likeCache = memoryCache.getLikeCache(category);

        log.info("viewCache: {}", viewCache.size());
        log.info("likeCache: {}", likeCache.size());

        String viewsKeys = VIEW_PREFIX + category + ": ";
        String likesKeys = LIKE_PREFIX + category + ": ";

        viewCache.forEach((keys, value) -> {
            log.info("viewKeys ={}", keys);
            Long targetId = Long.parseLong(keys.replace(viewsKeys, "").trim());
            Double score = redisTemplate.opsForZSet().score(zSetKey, targetId);
            double resultScore = (score == null ? 0 : score) + value;
            redisTemplate.opsForZSet().add(zSetKey, targetId, resultScore);
        });

        likeCache.forEach((keys, value) -> {
            log.info("keys ={}", keys);
            long targetLikeId = Long.parseLong(keys.replace(likesKeys, "").trim());
            Double score = redisTemplate.opsForZSet().score(zSetKey, targetLikeId);
            double resultScore = (score == null ? 0 : score) + value;
            redisTemplate.opsForZSet().add(zSetKey, targetLikeId, resultScore);
        });
    }

    /**
     * 인기 순위 상위 5개를 조회
     */
    public Set<Long> getZSetPopularity(String category) {
        String key = category + "_Ranking";

        Set<Long> longs = redisTemplate.opsForZSet().reverseRange(key, 0, 4);
        Set<ZSetOperations.TypedTuple<Long>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 4);

        result.forEach(item -> {
            System.out.println("popularNumber: " + item.getValue() + ", Score: " + item.getScore());
        });

        longs.forEach(number -> {
            System.out.println("popularNumber" + number);
        });

        return longs;
    }

    /**
     * 조회수를 DB에 반영하고 Redis에서 차감
     */
    private void updateViewDB(String redisKey, Long count, Long targetId, String str) {
        log.info("count = {}, targetId = {}, str = {}", count, targetId, str);
        portFolioRedisService.incrementHits(count, targetId);
        memoryCache.put(str + targetId, count);
        redisTemplate.opsForValue().decrement(redisKey, count);
    }

    /**
     * Redis에서 해당 패턴에 맞는 키 삭제
     */
    public void deleteKeyExceptionPattern(String pattern, String userKeySuffix) {
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            Set<String> keyDelete;
            if (userKeySuffix == null) {
                keyDelete = keys;
            } else {
                keyDelete = keys.stream()
                        .filter(key -> !key.contains(userKeySuffix))
                        .collect(Collectors.toSet());
            }
            if (!keyDelete.isEmpty()) {
                redisTemplate.delete(keyDelete);
            }
        }
    }

    public void deleteKeyIncludePattern(String pattern, String userKeySuffix){
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            Set<String> keyDelete;
            if (userKeySuffix == null) {
                keyDelete = keys;
            } else {
                keyDelete = keys.stream()
                        .filter(key -> key.contains(userKeySuffix))
                        .collect(Collectors.toSet());
            }
            if (!keyDelete.isEmpty()) {
                redisTemplate.delete(keyDelete);
            }
        }
    }

    public Long zSetSize(String category){
        String zSetKey = category + "_Ranking";
        return redisTemplate.opsForZSet().size(zSetKey);
    }

    /**
     * 카테고리 아이디에 대한 좋아요 정보를 DB에 반영
     */
    private void insertLikeDB(String category, String redisKey, long targetId, String str) {
        Set<Long> userIds = redisTemplate.opsForSet().members(redisKey + "_user");
        Long count = redisTemplate.opsForValue().get(redisKey);

        log.info("targetId= {}", targetId);
        log.info("count = {}", count);

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<LikeDto> list = new ArrayList<>();
        userIds.forEach(userId -> {
            list.add(new LikeDto(targetId, userId, LikeType.findLike(category)));
        });

        redisTemplate.opsForValue().decrement(redisKey, count);
        memoryCache.put(str + targetId, count);
        likeService.bulkSaveLike(list);
    }

    public RedisTemplate<String, Long> getRedisTemplate() {
        return redisTemplate;
    }
}


