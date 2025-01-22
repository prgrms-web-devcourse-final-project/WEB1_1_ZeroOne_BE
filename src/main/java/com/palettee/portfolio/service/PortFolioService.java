package com.palettee.portfolio.service;

import com.palettee.global.redis.service.RedisService;
import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.service.NotificationService;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.exception.PortFolioNotFoundException;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private final RedisTemplate<String, Object> redisTemplateForTarget;



    public CustomOffSetResponse findAllPortFolio(
            Pageable pageable,
            String majorJobGroup,
            String minorJobGroup,
            String sort,
            Optional<User> user,
            boolean isFirstPage

    ) {

        if(isFirstPage){
            CustomOffSetResponse cachedFirstPage = getCachedFirstPage(pageable);

            if(cachedFirstPage != null){
                cacheInRedisIsLiked(user, cachedFirstPage.content());
                return cachedFirstPage;
            }
            CustomOffSetResponse response = portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);
            log.info("캐시에 데이터 없음");
            hasNext =  response.hasNext();
            log.info("hasNext ={} ", hasNext);
            List<PortFolioResponse> results = response.content();

            results.forEach(result ->
                    redisTemplate.opsForZSet().add(RedisConstKey_PortFolio, result, TypeConverter.LocalDateTimeToDouble(result.getCreateAt()))
            );
            portFolio_Page_Size = pageable.getPageSize();

            redisTemplate.expire(RedisConstKey_PortFolio, 1, TimeUnit.HOURS); // 6시간으로 고정
            cacheInRedisIsLiked(user, response.content());
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
        PortFolio userPortFolio = getUserPortFolio(portFolioId);
        // 이미 DB에 반영된 좋아요 디비에서 삭제
        if(!flag){
           likeRepository.deleteAllByTargetId(user.getId(), portFolioId, LikeType.PORTFOLIO);
        }
        notificationService.send(NotificationRequest.like(portFolioId, user.getName(), userPortFolio.getUser().getName() + "의 포트폴리오", portFolioId, LikeType.PORTFOLIO));
        return redisService.likeCount(portFolioId, user.getId(),"portFolio");
    }

    public CustomPortFolioResponse findListPortFolio(
            Pageable pageable,
            Long userId,
            Long likeId
    ) {
        return portFolioRepository.PageFindLikePortfolio(pageable, userId, likeId);
    }

    public PortFolioWrapper popularPf(Optional<User> user){
        PortFolioWrapper portFolioWrapper = popularPortFolio(user);
        if(user.isPresent()){
            log.info("user가 들어옴");
            Set<Long> portFolioIds = redisService.getLikeTargetIds(user.get().getId(), "portFolio");

            if(portFolioIds.isEmpty()){
                log.info("유저가 누른 아이디가 없음");
            }

            portFolioWrapper.portfolioResponses()
                    .stream()
                    .forEach(portFolioPopularResponse -> {
                        boolean isLiked = portFolioIds != null && portFolioIds.contains(portFolioPopularResponse.getPortFolioId());
                        portFolioPopularResponse.setLiked(isLiked);
                    });

            return portFolioWrapper;
        }
        return portFolioWrapper;
    }

    /**
     * 상위 5개 레디스에 캐싱
     * @return
     */
    public PortFolioWrapper popularPortFolio(Optional<User> user) {
        String zSetKey = "portFolio_Ranking";

        List<PortFolioPopularResponse> listFromRedis = getListFromRedis(zSetKey);
        user.ifPresent(u -> {
            List<Long> longs = listFromRedis
                    .stream()
                    .map(PortFolioPopularResponse::getPortFolioId)
                    .toList();

            Set<Long> portFolioIds = likeRepository.findByTargetIdAndTarget(user.get().getId(),LikeType.PORTFOLIO ,longs);

            if (portFolioIds.isEmpty()) {
                log.info("유저가 누른 아이디가 없음");
            }

            listFromRedis.forEach(response -> {
                response.setLiked(portFolioIds.contains(response.getPortFolioId()));
            });
        });
        return new PortFolioWrapper(listFromRedis);
        }

    @SuppressWarnings("unchecked")
    public List<PortFolioPopularResponse> getListFromRedis(String zSetKey) {
        Object result = redisTemplateForTarget.opsForValue().get(zSetKey);
        if (result instanceof List) {
            return (List<PortFolioPopularResponse>) result; // List<PortFolioPopularResponse>로 캐스팅
        }
        return Collections.emptyList(); // 빈 리스트 반환
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

    private void cacheInRedisIsLiked(Optional<User> user, List<PortFolioResponse> portFolioResponses){
        user.ifPresent(u ->{
            List<Long> longs = portFolioResponses.stream()
                    .map(PortFolioResponse::getPortFolioId)
                    .toList();

            Set<Long> portFolioIds = likeRepository.findByTargetIdAndTarget(u.getId(),LikeType.PORTFOLIO ,longs);

            portFolioResponses.forEach(portFolioResponse -> {
                portFolioResponse.setLiked(portFolioIds.contains(portFolioResponse.getPortFolioId()));
            });
        });
    }


}
