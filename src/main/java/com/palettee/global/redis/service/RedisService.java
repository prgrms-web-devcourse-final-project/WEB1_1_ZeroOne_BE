package com.palettee.global.redis.service;

import com.palettee.global.cache.MemoryCache;
import com.palettee.likes.controller.dto.LikeDto;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.service.LikeService;
import com.palettee.portfolio.controller.dto.response.PortFolioPopularResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.repository.PortFolioRepository;
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

    private final PortFolioRepository portFolioRepository;
    private final MemoryCache memoryCache;
    private final LikeService likeService;

    private final RedisTemplate<String, PortFolioPopularResponse> responseRedisTemplate;
    /**
     * redis에 해당 뷰 카운팅
     * @param targetId 는 achieveId, portFolioId
     * @param category 는 achieve, portFolio
     *
     *   key = targetId의 view Count 용
     *   userKey = 중복 조회수 방지를 위해 targetId를 조회한 유저 Id가 value
     */
    public boolean viewCount(Long targetId, Long userId ,String category) {
        String key = VIEW_PREFIX + category + ": " + targetId;
        String userKey = key + "_user";


        // 해당 카테고리의 아이디에 대한 유저가 존재하는지 여부  -> 존재하면 해당 목록에 저장 후 카운팅, 존재 x 노 카운팅
        Long validation = redisTemplate.opsForSet().add(userKey, userId);

        if(validation != null && validation > 0) {
            log.info("조회수 카운팅");
            redisTemplate.opsForValue().increment(key, 1L);

            return true;
        }
        log.info("24시간이 지나야 조회 할 수 있습니다");
        return false;

    }

    /**
     * 좋아요 카운트 처리
     * @param targetId 는 achieveId, portFolioId, gatheringId
     * @param userId 유저 아이디
     * @param category achieve, portFolio, gathering
     *
     *   key = targetId의 like Count
     *   userKey = 좋아요 취소를 위해 targetId를 조회한 유저 Id가 value
     *
     *   이미 유저가 좋아요를 누른 게시물을 확인하고 좋아요를 눌렀을시에
     */
    public boolean likeCount(Long targetId, Long userId, String category) {
        String key = LIKE_PREFIX + category + ": " + targetId;
        String userKey = key + "_user";
        String userTargetsKey = LIKE_PREFIX + category + ": " + userId + "_targets"; // userId가 좋아요를 누른 모든 targetId를 추적

        Long validation = redisTemplate.opsForSet().add(userKey, userId);

        if (validation != null && validation > 0) {
            log.info("좋아요를 눌렀습니다");
            redisTemplate.opsForValue().increment(key, 1L);
            redisTemplate.opsForSet().add(userTargetsKey, targetId);
            return true;
        } else {
            log.info("좋아요를 취소하였습니다");
            redisTemplate.opsForSet().remove(userKey, userId);
            redisTemplate.opsForValue().set(key, 0L); //decrement 를 안한 이유: count redis 가 reset 된 후에 좋아요 취소하면 -1 값이 들어감
            redisTemplate.opsForSet().remove(userTargetsKey, targetId);
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
        Set<String> redisKeys = redisTemplate.keys(viewKeys + "*");  //모든 VIEW_PREFIX 패턴을 가져옴

        if (redisKeys.isEmpty()) {
            log.info("해당 키에 대한 값이 없음");
            return;
        }

        redisKeys = redisKeys.stream()   // SET에 있는 USER 조회용 키들은 제외시킴
                .filter(redisKey -> !redisKey.contains("_user"))
                .collect(Collectors.toSet());

        redisKeys.forEach(redisKey -> {
            log.info("카운트");
            long targetId = Long.parseLong(redisKey.replace(viewKeys, "").trim()); // 키에 있는 targetId 들을 가져옴
            Long countViews = Optional.ofNullable(redisTemplate.opsForValue().get(redisKey)).orElse(0L); // view count 인 value 를 가져옴
            log.info("targetId: {}, countViews: {}", targetId, countViews);

            if (countViews > 0) {
                updateViewDB(redisKey, countViews, targetId); //배치 업데이트
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
                .filter(redisKey -> !redisKey.contains("_user") && !redisKey.contains("_targets"))
                .collect(Collectors.toSet());

        redisKeys.forEach(redisKey -> {
            log.info("redisKey ={}", redisKey);
            long targetId = Long.parseLong(redisKey.replace(likeKeys, "").trim());
            Long countLikes = Optional.ofNullable(redisTemplate.opsForValue().get(redisKey)).orElse(0L); // view count 인 value 를 가져옴
            if(countLikes > 0) {
                // 배치 insert
                insertLikeDB(category, redisKey, targetId);
            }
        });
    }

    /**
     * 카테고리의 순위를 Redis에서 계산하여 zset에 반영
     */
    public void rankingCategory(String category) {
        String zSetKey = category + "_Ranking";

        log.info("memort CacheSize ={}", memoryCache.getLocalCache().size());

        //memory cache 있을시에 한번 비우기
        if(memoryCache.getLocalCache() != null && !memoryCache.getLocalCache().isEmpty()){
            responseRedisTemplate.opsForZSet().removeRange(zSetKey, 0, -1);
        }


        Map<String, Long> viewCache = memoryCache.getViewCache(category);
        Map<String, Long> likeCache = memoryCache.getLikeCache(category);

        log.info("viewCache: {}", viewCache.size());
        log.info("likeCache: {}", likeCache.size());

        String viewsKeys = VIEW_PREFIX + category + ": ";
        String likesKeys = LIKE_PREFIX + category + ": ";

        // 아이디를 통해 db에서 한번에 조회하기 위해서
        Set<Long> setList = new HashSet<>();

        //set에 가중치에 key에 대한 아이디들 저장
        viewCache.forEach((keys, value) -> {
            Long targetId = Long.parseLong(keys.replace(viewsKeys, "").trim());
            setList.add(targetId);
        });

        //set에 가중치에 key에 대한 아이디들 저장
        likeCache.forEach((keys, value) -> {
            Long targetId = Long.parseLong(keys.replace(likesKeys, "").trim());
            setList.add(targetId);
        });

        if(!setList.isEmpty()) {
            //아이디들을 통해 포트폴리오들을 가져옴
            List<PortFolio> portFolios = portFolioRepository.findAllByPortfolioIdIn(new ArrayList<>(setList));

            // 해당 아이디에 대한 포트폴리오 매핑
            Map<Long, PortFolio> collect = portFolios.stream()
                    .collect(Collectors.toMap(PortFolio::getPortfolioId, portFolio -> portFolio));

            //가중치
            viewCache.forEach((keys, value) -> {
                Long targetId = Long.parseLong(keys.replace(viewsKeys, "").trim());
                PortFolio portFolio = collect.get(targetId);
                if(portFolio != null){
                    Double score = responseRedisTemplate.opsForZSet().score(zSetKey, PortFolioPopularResponse.toDto(portFolio));
                    log.info("viewScore={}", score);
                    double result = ((score == null) ? 0 : score) + value;
                    responseRedisTemplate.opsForZSet().add(zSetKey, PortFolioPopularResponse.toDto(portFolio), result);
                }
            });

            likeCache.forEach((keys, value) -> {
                Long targetId = Long.parseLong(keys.replace(likesKeys, "").trim());
                log.info("targetId ={}", targetId);
                PortFolio portFolio = collect.get(targetId);
                if(portFolio != null) {
                    log.info("값이 잇음");
                    Double score = responseRedisTemplate.opsForZSet().score(zSetKey, PortFolioPopularResponse.toDto(portFolio));
                    log.info("LikeScore={}", score);
                    double result = ((score == null) ? 0 : score) + value;
                    responseRedisTemplate.opsForZSet().add(zSetKey, PortFolioPopularResponse.toDto(portFolio), result);
                }
            });

        }
    }

    /**
     * 인기 순위 상위 5개 targetId와 점수조회
     */
    public Map<Long, Double> getZSetPopularity(String category) {
        String key = category + "_Ranking";

        Set<ZSetOperations.TypedTuple<Long>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 4);

        // 로그로 데이터 출력
        typedTuples.forEach(tuple -> {
            System.out.println("Value: " + tuple.getValue() + ", Score: " + tuple.getScore());
        });

        return typedTuples.stream()
                .collect(Collectors.toMap(
                        ZSetOperations.TypedTuple::getValue,
                        ZSetOperations.TypedTuple::getScore,
                        (e1, e2) -> e1, // 중복된 key 처리 방식 (여기서는 충돌이 없을 것으로 가정)
                        LinkedHashMap::new  // LinkedHashMap으로 반환하여 순서 유지
                ));

    }

    /**
     *
     * @param redisKey -> view Count 용 키를 가져옴
     * @param count  -> count 횟수
     * @param targetId -> target Id
     */
    private void updateViewDB(String redisKey, Long count, Long targetId) {
        log.info("count = {}, targetId = {}, str = {}", count, targetId);
        portFolioRedisService.incrementHits(count, targetId); //배치 업데이트
        memoryCache.put(redisKey, count); // 해당 대한 가중치 넣어줌
        redisTemplate.opsForValue().decrement(redisKey, count); // view(count) 만큼 차감
    }

    /**
     *
     * @param pattern -> 삭제하고 싶은 패턴 지정
     * @param userKeySuffix -> 패턴들중 상세 패턴 지정
     *
     *  상세 패턴 제외한 패턴
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
    public void deleteKeyExceptionPatterns(String pattern, String userKeySuffix, String targetKeySuffix) {
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            Set<String> keyDelete;
            if (userKeySuffix == null) {
                keyDelete = keys;
            } else {
                keyDelete = keys.stream()
                        .filter(key -> !key.contains(userKeySuffix) && !key.contains(targetKeySuffix))
                        .collect(Collectors.toSet());
            }
            if (!keyDelete.isEmpty()) {
                redisTemplate.delete(keyDelete);
            }
        }
    }

    /**
     *
     * @param pattern -> 삭제하고 싶은 패턴 지정
     * @param userKeySuffix -> 패턴들중 상세 패턴 지정
     *
     *    상세 패턴 적용한 패턴
     */

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

    /**
     *
     * @param category
     * @return 해당 zetSize 반환
     */
    public Long zSetSize(String category){
        String zSetKey = category + "_Ranking";
        return redisTemplate.opsForZSet().size(zSetKey);
    }
    /**
     *
     * @param category
     * @param targetId
     * @param userId
     * @return 이미 좋아요가 Set 에 targetId와 UserId가 존재하면 -> 즉 이미 좋아요를 누른 USER가 존재하고 count redis 안의 값이 1인 유저 -> 아직 db에 반영이 되지 않음
     *  이미 좋아요가 되어 있는 멤버중 likeCount 가 0이거나 -> 이미 디비에 반영되거나 키가 시간이 지나 삭제된 애들은 DB에서 좋아요 삭제 하기 위해 false 반환 -> DB에 반영이됨
     *
     *  혹시나 Set User 의 키가 사라졌을때는 DB에서 해당 좋아요 삭제 시켜주고 다시 값을 넣어줌
     *
     */

    public Boolean likeExistInRedis(String category, Long targetId, Long userId) {
        // Redis 키 생성
        String baseKey = LIKE_PREFIX + category + ": " + targetId;
        String userKey = baseKey + "_user";

        // Redis 데이터 조회
        boolean isUserLiked = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userKey, userId));
        Long likeCount = redisTemplate.opsForValue().get(baseKey);

        return isUserLiked && (likeCount != null && likeCount > 0);
    }

    public boolean redisInLikeUser(String category, Long targetId, Long userId) {
        String baseKey = LIKE_PREFIX + category + ": " + targetId;
        String userKey = baseKey + "_user";

        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userKey, userId));
    }


    public Long likeCountInRedis(String category, Long targetId){
        String key = LIKE_PREFIX + category + ": " + targetId;

        Long likeCount = redisTemplate.opsForValue().get(key);

        if(likeCount != null && likeCount > 0){
            log.info("likeCount = {}", likeCount);
            return likeCount;
        }
        return null;
    }

    public Set<Long> getLikeTargetIds(Long userId, String category){
        return redisTemplate.opsForSet().members( LIKE_PREFIX + category + ": " + userId + "_targets");
    }


    /**
     *
     * @param category
     * @param redisKey String 자료구조에 있는 key값
     * @param targetId categoryId
     */
    private void insertLikeDB(String category, String redisKey, long targetId) {
        Set<Long> userIds = redisTemplate.opsForSet().members(redisKey + "_user"); //해당 targetId에 좋아요를 누른 유저의 아이디들을 조회
        Long count = redisTemplate.opsForValue().get(redisKey); // 타겟 아이디들을 좋아요 수를 가져옴

        log.info("targetId= {}", targetId);
        log.info("count = {}", count);

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<LikeDto> list = new ArrayList<>();
        userIds.forEach(userId -> {
            list.add(new LikeDto(targetId, userId, LikeType.findLike(category))); // 해당 targetId에 대한 유저들을 list 로 뽑음
        });

        redisTemplate.opsForValue().decrement(redisKey, count); // 카운트 차감
        memoryCache.put(redisKey, count); // 가중치 저장
        likeService.bulkSaveLike(list);  // bulk insert
    }

    public RedisTemplate<String, Long> getRedisTemplate() {
        return redisTemplate;
    }
}


