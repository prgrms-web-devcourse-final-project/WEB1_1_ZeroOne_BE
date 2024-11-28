package com.palettee.gathering.repository;

import static com.palettee.gathering.domain.QGathering.*;
import static com.palettee.likes.domain.QLikes.*;
import static com.palettee.user.domain.QUser.*;

import com.palettee.gathering.controller.dto.Response.*;
import com.palettee.gathering.domain.Sort;
import com.palettee.gathering.domain.*;
import com.palettee.likes.domain.*;
import com.palettee.portfolio.controller.dto.response.*;
import com.palettee.user.controller.dto.response.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

@Repository
public class GatheringRepositoryImpl implements GatheringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public GatheringRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /*
    NoOffSet 방식울 활용한 포트폴리오 전체 조회
     */
    @Override
    public Slice<GatheringResponse> pageGathering(
            String sort,
            String period,
            String position,
            String status,
            Long gatheringId,
            Pageable pageable) {

        List<Gathering> result = queryFactory
                .selectFrom(gathering)
                .join(gathering.user, user).fetchJoin()
                .where(sortEq(sort), periodEq(period), positionEq(position), statusEq(status), pageIdLt(gatheringId))
                .orderBy(gathering.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = hasNextPage(pageable, result);

        List<GatheringResponse> list = result.stream()
                .map(GatheringResponse::toDto)
                .toList();

        return new SliceImpl<>(list, pageable, hasNext);
    }

    @Override
    public CustomSliceResponse PageFindLikeGathering(Pageable pageable, Long userId, Long likeId) {
        List<Long> longs = queryFactory
                .select(likes.targetId)
                .from(likes)
                .where(
                        likes.user.id.eq(userId)
                                .and(likes.likeType.eq(LikeType.GATHERING))
                                .and(likeIdEq(likeId))
                )
                .leftJoin(likes.user, user)
                .limit(pageable.getPageSize() + 1)
                .orderBy(likes.likeId.desc())
                .fetch();

        boolean hasNext = hasNextPage(pageable, longs);

        Long nextId = hasNext ? longs.get(longs.size() - 1) : null;


        List<GatheringResponse> list = queryFactory
                .selectFrom(gathering)
                .where(gathering.id.in(longs))
                .join(gathering.user, user).fetchJoin()
                .fetch()
                .stream()
                .map(GatheringResponse::toDto).toList();


        return new CustomSliceResponse(list, hasNext, nextId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetUserGatheringResponse findGatheringsOnUserWithNoOffset(
            Long userId, int size,
            Long gatheringOffset
    ) {

        // id 내림차순 (최신순) 정렬
        List<Gathering> searchResult = queryFactory
                .selectFrom(gathering)
                .where(
                        gathering.user.id.eq(userId),
                        gatheringOffset != null ?
                                gathering.id.loe(gatheringOffset) : null
                )
                .orderBy(gathering.id.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = searchResult.size() > size;
        Long nextOffset = null;

        if (hasNext) {
            nextOffset = searchResult.get(size).getId();
            searchResult = searchResult.stream()
                    .limit(size)
                    .toList();
        }

        return GetUserGatheringResponse.of(
                searchResult, hasNext, nextOffset
        );
    }

    private BooleanExpression sortEq(String sort) {
        return sort != null ? gathering.sort.eq(Sort.findSort(sort)) : null;
    }

    private BooleanExpression periodEq(String period) {
        return period != null ? gathering.period.eq(period) : null;
    }

    private BooleanExpression positionEq(String position) {
        return position != null ? gathering.position.eq(Position.findPosition(position)) : null;
    }

    private BooleanExpression statusEq(String status) {
        return status != null ? gathering.status.eq(Status.findsStatus(status)) : null;
    }

    private BooleanExpression pageIdLt(Long pageId) {
        return pageId != null ? gathering.id.lt(pageId) : null;
    }

    private BooleanExpression likeIdEq(Long likeId) {
        return likeId != null ? likes.likeId.lt(likeId) : null;
    }

    private static boolean hasNextPage(Pageable pageable, List<?> result) {
        boolean hasNext = false;

        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
            result.remove(pageable.getPageSize());
        }
        return hasNext;
    }
}

