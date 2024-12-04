package com.palettee.gathering.repository;


import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.palettee.likes.domain.LikeType;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.user.controller.dto.response.users.GetUserGatheringResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.palettee.gathering.domain.QGathering.gathering;
import static com.palettee.likes.domain.QLikes.likes;
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
    public CustomSliceResponse pageGathering(
            String sort,
            String subject,
            String period,
            String position,
            String status,
            Long gatheringId,
            Pageable pageable) {

        List<Gathering> result = queryFactory
                .selectFrom(gathering)
                .join(gathering.user, user).fetchJoin()
                .where(sortEq(sort),subjectEq(subject), periodEq(period), statusEq(status), pageIdLoe(gatheringId))
                .orderBy(gathering.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = hasNextPage(pageable, result);

        Long nextId = hasNext ? result.get(result.size() - 1).getId() : null;

        List<GatheringResponse> list = result.stream()
                .map(GatheringResponse::toDto)
                .toList();

        return new CustomSliceResponse(list, hasNext, nextId);
    }

    @Override
    public CustomSliceResponse PageFindLikeGathering(Pageable pageable, Long userId, Long likeId) {
        List<Tuple> results = queryFactory
                .select(likes.targetId, likes.likeId)
                .from(likes)
                .where(
                        likes.user.id.eq(userId)
                                .and(likes.likeType.eq(LikeType.GATHERING))
                                .and(likeIdLoe(likeId))
                )
                .leftJoin(likes.user, user)
                .orderBy(likes.likeId.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();


        List<Long> targetIds = results.stream()
                .map(result -> result.get(likes.targetId))
                .collect(Collectors.toList());// targetId 리스트 생성


        boolean hasNext = hasNextPage(pageable, targetIds);

        Long nextId = hasNext ? results.get(results.size() - 1).get(likes.likeId) : null;

        List<GatheringResponse> list = queryFactory
                .selectFrom(gathering)
                .where(gathering.id.in(targetIds))
                .join(gathering.user, user).fetchJoin()
                .fetch()
                .stream()
                .map(GatheringResponse::toDto).collect(Collectors.toList());

        list.sort(Comparator.comparingInt(item -> targetIds.indexOf(item.gatheringId())));


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

    private BooleanExpression subjectEq(String subject){
        return subject != null ? gathering.subject.eq(Subject.finSubject(subject)) : null;
    }

    private BooleanExpression periodEq(String period) {
        return period != null ? gathering.period.eq(period) : null;
    }
//
//    private BooleanExpression positionEq(String position) {
//        return position != null ? gathering.position.eq(Position.findPosition(position)) : null;
//    }

    private BooleanExpression statusEq(String status) {
        return status != null ? gathering.status.eq(Status.findsStatus(status)) : null;
    }

    private BooleanExpression pageIdLoe(Long pageId) {
        return pageId != null ? gathering.id.loe(pageId) : null;
    }

    private BooleanExpression likeIdLoe(Long likeId) {
        return likeId != null ? likes.likeId.loe(likeId) : null;
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


