package com.palettee.gathering.repository;

import com.palettee.gathering.controller.dto.Response.*;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.user.controller.dto.response.users.*;
import org.springframework.data.domain.*;

public interface GatheringRepositoryCustom {

    CustomSliceResponse pageGathering(
            String sort,
            String subject,
            String period,
            String position,
            String status,
            Long gatheringId,
            Pageable pageable
    );

    CustomSliceResponse PageFindLikeGathering(Pageable pageable, Long userId, Long likeId);

    /**
     * {@code NoOffset} 방식을 이용한 {@code 특정 유저의 게더링} 목록 조회 메서드
     * <p>
     * {@code (ID <= gatheringOffset)} 인 {@code gathering} 들을 최신순 정렬 {@code (gatheringId 내림차순)} 로
     * 가져옴.
     *
     * @param userId          조회할 유저의 id
     * @param size            가져올 게더링 개수
     * @param gatheringOffset 이전 조회에서 제공된 {@code nextGatheringId}
     */
    GetUserGatheringResponse findGatheringsOnUserWithNoOffset(
            Long userId, int size,
            Long gatheringOffset
    );
}
