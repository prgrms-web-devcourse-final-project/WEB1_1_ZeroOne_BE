package com.palettee.portfolio.service;

import com.palettee.global.redis.service.RedisService;
import com.palettee.likes.domain.LikeType;
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

    private final RedisTemplate<String, Object> redisTemplateForTarget;



    public Slice<PortFolioResponse> findAllPortFolio(
            Pageable pageable,
            String majorJobGroup,
            String minorJobGroup,
            String sort,
            Optional<User> user
    ) {

        Slice<PortFolioResponse> portFolioResponses = portFolioRepository.PageFindAllPortfolio(pageable, majorJobGroup, minorJobGroup, sort);

        user.ifPresent(u-> {
            log.info("유저가 있습니다");

            List<Long> longs = portFolioResponses.stream()
                    .map(PortFolioResponse::getPortFolioId).toList();

            // 유저가 누른 좋아요들의 포트폴리오 아이디들을 DB에서 조회
            Set<Long> portFolioIds = likeRepository.findByTargetIdAndPortFolio(user.get().getId(), longs);

//            redisService.getLikedTargetId(user.get().getId(), "portFolio")
//                    .forEach(id -> portFolioIds.add(id));

            portFolioResponses.forEach(portFolios -> portFolios.setLiked(portFolioIds.contains(portFolios.getPortFolioId())));
        });
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
           likeRepository.deleteAllByTargetId(user.getId(), portFolioId, LikeType.PORTFOLIO);
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
        log.info("호출");
        String zSetKey = "portFolio_Ranking";

        List<PortFolioPopularResponse> listFromRedis = getListFromRedis(zSetKey);
        user.ifPresent(u -> {
            List<Long> longs = listFromRedis
                    .stream()
                    .map(PortFolioPopularResponse::getPortFolioId)
                    .toList();

            Set<Long> portFolioIds = likeRepository.findByTargetIdAndPortFolio(user.get().getId(), longs);

            if (portFolioIds.isEmpty()) {
                log.info("유저가 누른 아이디가 없음");
            }

            listFromRedis.forEach(response -> response.setLiked(portFolioIds.contains(response.getPortFolioId())));
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




    public PortFolio getPortFolio(Long portFolioId){
       return portFolioRepository.findById(portFolioId)
              .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
    }


}
