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

import java.util.List;
import java.util.Set;

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

    @Transactional
    public void clickPortFolio(Long portPolioId) {
        redisService.viewCount(portPolioId, "portFolio");
    }

    public CustomSliceResponse findListPortFolio(
            Pageable pageable,
            Long userId,
            Long likeId
    ) {
        return portFolioRepository.PageFindLikePortfolio(pageable, userId, likeId);
    }

    @Transactional
    public PortFolioLikeResponse createPortFolioLike(Long portfolioId, User user) {
        PortFolio portFolio = portFolioRepository.findById(portfolioId)
                .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
        if(cancelLike(portfolioId, user)) {
            return new PortFolioLikeResponse(null);
        }

        Likes likes = Likes.builder()
                .likeType(LikeType.PORTFOLIO)
                .user(user)
                .targetId(portfolioId)
                .build();

        Long targetId = portFolio.getUser().getId();
        notificationService.send(NotificationRequest.like(targetId, user.getName()));

        return PortFolioLikeResponse.toDTO(likeRepository.save(likes));
    }

    /**
     * 상위 5개 레디스에 캐싱
     * @return
     */
    @Cacheable(value = "portFolio_cache", key = "'portFolio_cache'")
    public List<PortFolioResponse> popularPortFolio(){
        Set<Long> portFolio = redisService.getZSetPopularity("portFolio");

       return portFolioRepository.findAllByPortfolioIdIn(portFolio)
               .stream()
               .map(PortFolioResponse::toDto).toList();

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
