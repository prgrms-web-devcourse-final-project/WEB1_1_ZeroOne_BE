package com.palettee.portfolio.service;

import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.notification.service.NotificationService;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioPopularResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioWrapper;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.exception.PortFolioNotFoundException;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortFolioService {

    private final PortFolioRepository portFolioRepository;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;

    private final RedisService redisService;



    public Slice<PortFolioResponse> findAllPortFolio(
            Pageable pageable,
            String majorJobGroup,
            String minorJobGroup,
            String sort,
            Optional<User> user
    ) {

        Slice<PortFolioResponse> portFolioResponses = portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);

        if(user.isPresent()){
            List<Long> longs = portFolioResponses.stream()
                    .map(PortFolioResponse::getPortFolioId).toList();

            // 유저가 누른 좋아요들의 포트폴리오 아이디들을 DB에서 조회
            List<Long> portFolioIds = likeRepository.findByTargetIdAndPortFolio(user.get().getId(), longs);

            portFolioResponses.forEach(portFolios -> {
                boolean isLiked = portFolioIds.contains(portFolios.getPortFolioId());
                portFolios.setLiked(isLiked);
            });

        }
        return portFolioResponses;
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

    public CustomSliceResponse findListPortFolio(
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




    private boolean cancelLike(Long portfolioId, User user) {
        List<Likes> findByLikes = likeRepository.findByList(user.getId(), portfolioId, LikeType.PORTFOLIO);

        if(findByLikes != null) {
            likeRepository.deleteAll(findByLikes);
            return true;
        }
        return false;
    }
}
