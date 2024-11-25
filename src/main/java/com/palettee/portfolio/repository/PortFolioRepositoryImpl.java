package com.palettee.portfolio.repository;

import com.palettee.likes.domain.LikeType;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.controller.dto.response.QPortFolioResponse;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
    public Slice<PortFolioResponse> PageFindAllPortfolio(Pageable pageable, String majorJobGroup, String minorJobGroup, String sort) {

        List<PortFolioResponse> result = queryFactory
                .select(new QPortFolioResponse(
                        portFolio.portfolioId,
                        portFolio.url,
                        user.name,
                        user.briefIntro,
                        user.majorJobGroup,
                        user.minorJobGroup,
                        user.imageUrl
                ))
                .from(portFolio)
                .leftJoin(portFolio.user, user)
                .where(majorJobGroupEquals(majorJobGroup),
                        minorJobEquals(minorJobGroup))
                .orderBy(sortType(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // 페이지 존재 여부를 나타내기 위해 하나 더 가져온걸 삭제
        boolean hasNext = hasNextPage(pageable, result);

        return new SliceImpl<>(result, pageable, hasNext);
    }


    /*
    좋아요한 포트폴리오 조회(noOffSet)
     */

    @Override
    public CustomSliceResponse PageFindLikePortfolio(Pageable pageable, Long userId, Long likeId) {

        /*
        NoOffset으로 먼저 targetId 들 조회 -> 유저가 좋아요한 포트폴리오 아이디들 조회
         */

        log.info("likeId = {}" , likeId);


        List<Long> longs = queryFactory
                .select(likes.targetId)
                .from(likes)
                .where(
                        likes.user.id.eq(userId)
                                .and(likes.likeType.eq(LikeType.PORTFOLIO))
                                .and(likeIdEq(likeId))
                )
                .leftJoin(likes.user, user)
                .orderBy(likes.likeId.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        log.info("log.size= {}", longs.size());

        boolean hasNext = hasNextPage(pageable, longs);

        Long nextId = hasNext ? longs.get(longs.size() - 1) : null;  // 다음 likeId를 조회하기 위해 NoOffSet 방식


        List<PortFolioResponse> result = queryFactory
                .select(new QPortFolioResponse(
                        portFolio.portfolioId,
                        portFolio.url,
                        user.name,
                        user.briefIntro,
                        user.majorJobGroup,
                        user.minorJobGroup,
                        user.imageUrl
                ))
                .from(portFolio)
                .leftJoin(portFolio.user, user)
                .where(portFolio.portfolioId.in(longs))
                .fetch();


        return new CustomSliceResponse(result, hasNext, nextId);
    }

    private BooleanExpression majorJobGroupEquals(String majorJobGroup) {

        MajorJobGroup majorGroup = MajorJobGroup.findMajorGroup(majorJobGroup);

        if(majorGroup != null){
            return user.majorJobGroup.eq(majorGroup);
        }

        return null;
    }

    private BooleanExpression minorJobEquals(String minorJobGroup) {

        MinorJobGroup findMinorJobGroup = MinorJobGroup.findMinorJobGroup(minorJobGroup);

        if(findMinorJobGroup != null){
            return user.minorJobGroup.eq(findMinorJobGroup);
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


