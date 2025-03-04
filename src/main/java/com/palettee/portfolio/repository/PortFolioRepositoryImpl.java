package com.palettee.portfolio.repository;

import com.palettee.likes.domain.LikeType;
import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.gathering.controller.dto.Response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.CustomPortFolioResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.palettee.likes.domain.QLikes.likes;
import static com.palettee.portfolio.domain.QPortFolio.portFolio;
import static com.palettee.user.domain.QUser.user;

@Slf4j
@Repository
public class PortFolioRepositoryImpl implements PortFolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PortFolioRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /*
     *
     * @전체 포트폴리오 조회(offset)
     *
     */
    @Override
    public CustomOffSetResponse PageFindAllPortfolio(Pageable pageable, String majorJobGroup, String minorJobGroup, String sort) {

        List<PortFolioResponse> result = queryFactory
                .select(portFolio
                )
                .from(portFolio)
                .leftJoin(portFolio.user, user).fetchJoin()
                .where(majorJobGroupEquals(majorJobGroup),
                        minorJobEquals(minorJobGroup))
                .orderBy(sortType(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch()
                .stream()
                .map(PortFolioResponse::toDto).collect(Collectors.toList());

        // 페이지 존재 여부를 나타내기 위해 하나 더 가져온걸 삭제
        boolean hasNext = hasNextPage(pageable, result);

        log.info("offset ={}", pageable.getOffset());


        return CustomOffSetResponse.toDto(result, hasNext, pageable.getOffset(), pageable.getPageSize());
    }
    /*
    좋아요한 포트폴리오 조회(noOffSet)
     */

    @Override
    public CustomPortFolioResponse PageFindLikePortfolio(Pageable pageable, Long userId, Long likeId) {

        /*
        NoOffset으로 먼저 targetId 들 조회 -> 유저가 좋아요한 포트폴리오 아이디들 조회
         */


        List<Tuple> results = queryFactory
                .select(likes.targetId, likes.likeId)
                .from(likes)
                .where(
                        likes.user.id.eq(userId)
                                .and(likes.likeType.eq(LikeType.PORTFOLIO))
                                .and(likeIdLoe(likeId)) 
                )
                .leftJoin(likes.user, user)
                .orderBy(likes.createAt.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<Long> targetIds = results.stream()
                .map(result -> result.get(likes.targetId))
                .collect(Collectors.toList());// targetId 리스트 생성


        boolean hasNext = hasNextPage(pageable, targetIds);

        Long nextId = hasNext ? results.get(results.size() -1).get(likes.likeId) : null;  // 다음 likeId를 조회하기 위해 NoOffSet 방식


        List<PortFolioResponse> list = queryFactory
                .select(portFolio
                )
                .from(portFolio)
                .leftJoin(portFolio.user, user).fetchJoin()
                .where(portFolio.portfolioId.in(targetIds))
                .fetch()
                .stream()
                .map(PortFolioResponse::toDto).collect(Collectors.toList());

        list.sort(Comparator.comparingInt(item -> targetIds.indexOf(item.getPortFolioId())));


        return new CustomPortFolioResponse(list, hasNext, nextId);
    }

    private BooleanExpression majorJobGroupEquals(String majorJobGroup) {

        MajorJobGroup majorGroup = MajorJobGroup.findMajorGroup(majorJobGroup);

        if(majorGroup != null){
            return portFolio.majorJobGroup.eq(majorGroup);
        }

        return null;
    }

    private BooleanExpression minorJobEquals(String minorJobGroup) {

        MinorJobGroup findMinorJobGroup = MinorJobGroup.findMinorJobGroup(minorJobGroup);

        if(findMinorJobGroup != null){
            return portFolio.minorJobGroup.eq(findMinorJobGroup);
        }
        return null;
    }

    private OrderSpecifier<?> sortType(String type) {
        // PathBuilder로 해당 PortFolio 접근 가능
        PathBuilder<?> entityPath = new PathBuilder<>(PortFolio.class, "portFolio");

        if (type.equals("latest")) {
            // 최신순 정렬
            return new OrderSpecifier<>(Order.DESC, entityPath.get("createAt", LocalDateTime.class));
        } else if (type.equals("popularlity")) {
            // 인기순은 조회수를 기준으로
            return new OrderSpecifier<>(Order.DESC, entityPath.get("hits", Long.class));
        }

        // 기본 시간 오름차순 전체 조회
        return new OrderSpecifier<>(Order.ASC, entityPath.get("createAt", LocalDateTime.class));
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


