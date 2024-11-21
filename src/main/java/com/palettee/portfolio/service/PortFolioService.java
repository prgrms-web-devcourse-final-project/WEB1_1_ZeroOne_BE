package com.palettee.portfolio.service;

import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioLikeResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.exception.PortFolioNotFoundException;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortFolioService {

    private final PortFolioRepository portFolioRepository;
    private final LikeRepository likeRepository;

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
        PortFolio portFolio = portFolioRepository.findById(portPolioId)
                .orElseThrow(() -> PortFolioNotFoundException.EXCEPTION);
        portFolio.incrementHits();
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
        Likes likes = Likes.builder()
                .likeType(LikeType.PORTFOLIO)
                .user(user)
                .targetId(portfolioId)
                .build();

        return PortFolioLikeResponse.toDTO(likeRepository.save(likes));
    }
}
