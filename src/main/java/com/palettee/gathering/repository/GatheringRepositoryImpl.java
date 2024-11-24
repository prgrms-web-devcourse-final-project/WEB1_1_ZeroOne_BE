package com.palettee.gathering.repository;

import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.palettee.gathering.domain.QGathering.gathering;
import static com.palettee.user.domain.QUser.user;

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

    private static boolean hasNextPage(Pageable pageable, List<?> result) {
        boolean hasNext = false;

        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
            result.remove(pageable.getPageSize());
        }
        return hasNext;
    }
}

