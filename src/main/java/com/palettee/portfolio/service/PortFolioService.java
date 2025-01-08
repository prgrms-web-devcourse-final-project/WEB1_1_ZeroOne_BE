package com.palettee.portfolio.service;

import com.palettee.global.redis.service.RedisService;
import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.notification.service.NotificationService;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.exception.PortFolioNotFoundException;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.palettee.global.Const.portFolio_Page_Size;
import static com.palettee.portfolio.repository.PortFolioRedisRepository.RedisConstKey_PortFolio;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortFolioService {

    private final PortFolioRepository portFolioRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    private final RedisTemplate<String, PortFolioResponse> redisTemplate;


    private static boolean hasNext;

    private final RedisService redisService;


    public CustomOffSetResponse findAllPortFolio(
            Pageable pageable,
            String majorJobGroup,
            String minorJobGroup,
            String sort,
            boolean isFirstPage

    ) {

        if(isFirstPage){
            CustomOffSetResponse cachedFirstPage = getCachedFirstPage(pageable);

            if(cachedFirstPage != null){
                return cachedFirstPage;
            }
            CustomOffSetResponse response = portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);
            log.info("캐시에 데이터 없음");
            hasNext =  response.hasNext();
            log.info("hasNext ={} ", hasNext);
            List<PortFolioResponse> results = response.content();

            results.forEach(result ->
                    redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, result, TypeConverter.LocalDateTimeToDouble(result.createAt()))
            );
            portFolio_Page_Size = pageable.getPageSize();

            redisTemplate.expire(RedisConstKey_PortFolio, 1, TimeUnit.HOURS); // 6시간으로 고정
            return response;
        }
        return portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);
    }


    public boolean clickPortFolio(Long portPolioId, Long userId) {
       return redisService.viewCount(portPolioId,userId, "portFolio");
    }

    @Transactional
    public boolean likePortFolio(User user, Long portFolioId) {

        Boolean flag = redisService.likeExistInRedis("portFolio", portFolioId, user.getId());

        // 이미 DB에 반영된 좋아요 디비에서 삭제
        if(!flag){
            cancelLike(portFolioId, user);
        }
        return redisService.likeCount(portFolioId, user.getId(),"portFolio");
    }

    public CustomPortFolioResponse findListPortFolio(
            Pageable pageable,
            Long userId,
            Long likeId
    ) {
        return portFolioRepository.PageFindLikePortfolio(pageable, userId, likeId);
    }

    /**
     * 상위 5개 레디스에 캐싱
     * @return
     */
    @Cacheable(value = "pf_cache", key = "'cache'")
    public PortFolioWrapper popularPortFolio(){

        Map<Long, Double> portFolioMap = redisService.getZSetPopularity("portFolio");

        Set<Long> longs = portFolioMap.keySet();

        List<Long> sortedPortFolio = new ArrayList<>(longs);

        List<PortFolio> portfolios = portFolioRepository.findAllByPortfolioIdIn(sortedPortFolio);


        // in 절을 사용하면 정렬 순서가 바뀌기 때문에 Map으로 순서를 맞춰줌
        Map<Long, PortFolio> portfoliosMap = portfolios.stream()
                .collect(Collectors.toMap(PortFolio::getPortfolioId, portfolio -> portfolio));

        // 본 List의 본 순서와 맞는 점수 대응
        List<PortFolioPopularResponse> list = sortedPortFolio.stream()
                .map(portFolioId -> {
                    Double score = portFolioMap.get(portFolioId);
                    PortFolio portFolio = portfoliosMap.get(portFolioId);
                    return portFolio != null ? PortFolioPopularResponse.toDto(portFolio, score) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PortFolioWrapper(list);
    }


    public PortFolio getPortFolio(Long portFolioId){
       return portFolioRepository.findById(portFolioId)
              .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
    }

    public PortFolio getUserPortFolio(Long portFolioId){
        return portFolioRepository.findByFetchUserPortFolio(portFolioId)
                .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
    }

    private CustomOffSetResponse getCachedFirstPage(Pageable pageable){
        Set<PortFolioResponse> range = redisTemplate.opsForZSet().reverseRange(RedisConstKey_PortFolio, 0, pageable.getPageSize());

        if(range != null && !range.isEmpty()){
            log.info("캐시에 값이 잇음");
            List<PortFolioResponse> portFolioResponses= new ArrayList<>(range);

            if(portFolioResponses.size() != pageable.getPageSize()){ //페이지 사이즈가 바뀌면
                log.info("range.size = {}", portFolioResponses.size());
                log.info("pageable.getPageSize = {}", pageable.getPageSize());
                log.info("사이즈가 다름");
                redisTemplate.delete(RedisConstKey_PortFolio);
                return null;
            }


            return new CustomOffSetResponse(portFolioResponses,hasNext,0L, portFolioResponses.size());
        }
        return null;
    }




    private boolean cancelLike(Long portfolioId, User user) {
        List<Likes> findByLikes = likeRepository.findByList(user.getId(), portfolioId, LikeType.PORTFOLIO);

        if(findByLikes != null) {
            likeRepository.deleteAll(findByLikes);
            return true;
        }
        return false;
    }
}
