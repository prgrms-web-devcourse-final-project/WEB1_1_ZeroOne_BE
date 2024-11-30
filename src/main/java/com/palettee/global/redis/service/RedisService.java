package com.palettee.global.redis.service;

import com.palettee.global.cache.MemoryCache;
import com.palettee.likes.controller.dto.LikeDto;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.service.LikeService;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Long> redisTemplate;

    private final PortFolioRepository portFolioRepository;


    private final MemoryCache memoryCache;

    private final LikeService likeService;

    /*
    Redis 를 사용한 view Count +1
     */
    public void viewCount(Long id, String category){
        String key = VIEW_PREFIX + category + ": " + id;
        redisTemplate.opsForValue().increment(key, 1L);;
    }

    /*
    Redis 를 사용하여 like Count +1
     */
    public void likeCount(Long id, Long userId, String category){
        String key = LIKE_PREFIX + category + ": " + id;
        String userKey = key +"_user";

        // 중복 좋아요 방지 -> 해당 유저가 좋아요를 눌렀는지 검증
        Long validation = redisTemplate.opsForSet().add(userKey, userId);

        // 만약 좋아요 성공시 좋아요 갯수 카운트
        if(validation != null && validation > 0){
            log.info("좋아요를 눌렀습니다");
            redisTemplate.opsForValue().increment(key, 1L);
        }
        // 이미 유저가 좋아요를 눌른 상태면 해당 좋아요 취소
        else{
            log.info("좋아요를 취소하였습니다");
            redisTemplate.opsForSet().remove(userKey, userId);
            redisTemplate.opsForValue().decrement(key, 1L);
        }
    }

    /*
    Redis 내에 있는
     */

    public void categoryToDb(String category){
        // category에 대한
        String viewKeys = VIEW_PREFIX + category + ": ";

        String likeKeys = LIKE_PREFIX + category + ": ";

        viewRedisToDB(viewKeys);
        likeRedisToDB(likeKeys, category);

    }


    public void rankingCategory(String category){
        String zSetKey = category + "_Ranking";
        redisTemplate.opsForZSet().removeRange(zSetKey, 0, -1); // 인기 순위 목록 한번 비우기

        //
        Map<String, Long> viewCache = memoryCache.getViewCache(category);
        Map<String, Long> likeCache = memoryCache.getLikeCache(category);

        String viewsKeys = VIEW_PREFIX + category + ": ";
        String likesKeys = LIKE_PREFIX + category + ": ";

        viewCache.forEach((keys, value) -> {
            // key에 대한 String은 버리고 id만 빼옴
            Long targetViewId = Long.parseLong((keys.replace(viewsKeys, "")));

            // zset의 id에 대한 score
            Double score = redisTemplate.opsForZSet().score(zSetKey, targetViewId);

            // score가 null 이면 누적 점수 합산 그게 아니면 새로운 점수로 초기화
            double resultScore = (score == null ? 0 : score) + value;

            redisTemplate.opsForZSet().add(zSetKey, targetViewId, resultScore); // zset에 해당 포트폴리오에 대한 id와 value 저장
        });

        likeCache.forEach((keys, value) -> {
            long targetLikeId = Long.parseLong((keys.replace(likesKeys, "")));

            // zset의 id에 대한 score
            Double score = redisTemplate.opsForZSet().score(zSetKey, targetLikeId);

            // score가 null 이면 누적 점수 합산 그게 아니면 새로운 점수로 초기화
            double resultScore = (score == null ? 0 : score) + value;

            redisTemplate.opsForZSet().add(zSetKey, targetLikeId, resultScore); // zset에 해당 포트폴리오에 대한 id와 value 저장
        });
    }

    @Transactional
    public void viewRedisToDB(String viewKeys) {
        Set<String> redisKeys = redisTemplate.keys(viewKeys + "*");

        // 카운팅 된 값이 아예 없으면 return
        if(redisKeys.isEmpty()){
            log.info("해당 키에  대한 값이 없음");
            return;
        }

        // View 의 키를 다 뒤짐
        redisKeys.forEach(redisKey -> {

            log.info("카운트");
            // 해당 키에 대한 아이디만 빼냄
            long targetId = Long.parseLong(redisKey.replace(viewKeys, ""));
            // 키를 하나씩 가져와서 증가된 조회수를 가져옴 만약에 아이디에 대하 조회수가 없을시 0을 반환
            Long countViews = Optional.ofNullable(redisTemplate.opsForValue().get(redisKey)).orElse(0L);

            log.info("targetId: {}, countViews: {}", targetId, countViews);

            if(countViews > 0){
                updateViewDB(redisKey, countViews ,targetId, viewKeys);
            }
        });
    }

    /**
     *
     * @param likeKeys :  고정 상수 값 + 카테고리:
     * @param category : 카테고리 : ex: portFolio, archive, gathering
     */

    public void likeRedisToDB(String likeKeys, String category) {
        //키 중에 해당 패턴인 애들 다꺼내줌
        Set<String> redisKeys = redisTemplate.keys(likeKeys + "*");

        if(redisKeys.isEmpty() || redisKeys == null){
            return;
        }

        // :user:가 포함되지 않은 키만 필터링 -> 유저 키가 포함되지 않은 애들
        redisKeys = redisKeys.stream()
                .filter(redisKey -> !redisKey.contains("_user")) // :user:가 포함된 키 제외
                .collect(Collectors.toSet());

        redisKeys.forEach(redisKey -> {

            log.info("redisKey ={}", redisKey);
            log.info("likeKeys ={}", likeKeys);

            long targetId = Long.parseLong(redisKey.replace(likeKeys, "").trim());

            // 해당 category에 좋아요를 누른 유저를 모두 꺼냄
            Set<Long> userIds = redisTemplate.opsForSet().members(redisKey + "_user");

            if(userIds == null || userIds.isEmpty()){
                return;
            }

            List<LikeDto> list = new ArrayList<>();

            userIds.forEach(userId -> {
                list.add(new LikeDto(targetId, userId, LikeType.findLike(category)));
            });

            likeService.bulkSaveLike(list);
        });
    }


     public void updateViewDB(String redisKey, Long count, Long targetId, String str){

        log.info("count = {}, targetId = {}, str = {}", count, targetId, str);
        // DB에 Redis 의 조회수 반영
        portFolioRepository.incrementHits(count, targetId);

        // 로컬 메모리 캐시에 디비에 반영된
        memoryCache.put(str + targetId, count);

        // Redis에 증가된 조회 수 차감 -> 데이터 정합성 때문에 해당 아이디 조회수 삭제
        redisTemplate.opsForValue().decrement(redisKey, count);
    }


    public RedisTemplate<String, Long> getRedisTemplate(){
        return redisTemplate;
    }


}
