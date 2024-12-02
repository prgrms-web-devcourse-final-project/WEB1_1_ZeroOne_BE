package com.palettee.portfolio.service;

import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.service.NotificationService;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioLikeResponse;
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
            String sort
    ) {
        return portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);
    }


    public void clickPortFolio(Long portPolioId, Long userId) {
        redisService.viewCount(portPolioId,userId, "portFolio");
    }

    public boolean likePortFolio(User user, Long portFolioId) {
       return redisService.likeCount(portFolioId, user.getId(),"portFolio");
    }

    public CustomSliceResponse findListPortFolio(
            Pageable pageable,
            Long userId,
            Long likeId
    ) {
        return portFolioRepository.PageFindLikePortfolio(pageable, userId, likeId);
    }

//    @Transactional
//    public PortFolioLikeResponse createPortFolioLike(Long portfolioId, User user) {
//        PortFolio portFolio = portFolioRepository.findById(portfolioId)
//                .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
//        if(cancelLike(portfolioId, user)) {
//            return new PortFolioLikeResponse(null);
//        }
//
//        Likes likes = Likes.builder()
//                .likeType(LikeType.PORTFOLIO)
//                .user(user)
//                .targetId(portfolioId)
//                .build();
//
//        Long targetId = portFolio.getUser().getId();
//        notificationService.send(NotificationRequest.like(targetId, user.getName()));
//
//        return PortFolioLikeResponse.toDTO(likeRepository.save(likes));
//    }

    /**
     * 상위 5개 레디스에 캐싱
     * @return
     */
    @Cacheable(value = "portFolio_cache", key = "'cache'")
    public PortFolioWrapper popularPortFolio(){
        Set<Long> portFolio = redisService.getZSetPopularity("portFolio");
        List<Long> sortedPortFolio = new ArrayList<>(portFolio);

        List<PortFolio> portfolios = portFolioRepository.findAllByPortfolioIdIn(sortedPortFolio);


        // in 절을 사용하면 정렬 순서가 바뀌기 때문에 Set으로 정렬한 순서랑 맞춰줘야함
        Map<Long, PortFolio> portfoliosMap = portfolios.stream()
                .collect(Collectors.toMap(PortFolio::getPortfolioId, portfolio -> portfolio));

        List<PortFolioResponse> list = sortedPortFolio.stream()
                .map(portfoliosMap::get)
                .filter(Objects::nonNull)
                .map(PortFolioResponse::toDto)
                .collect(Collectors.toList());

        return new PortFolioWrapper(list);
    }



    private boolean cancelLike(Long portfolioId, User user) {
        Likes findByLikes = likeRepository.findByUserIdAndTargetId(user.getId(), portfolioId,LikeType.PORTFOLIO);

        if(findByLikes != null) {
            likeRepository.delete(findByLikes);
            return true;
        }
        return false;
    }
}
