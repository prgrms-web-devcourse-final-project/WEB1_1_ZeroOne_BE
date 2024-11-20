package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.controller.dto.QPortFolioResponseDTO;
import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static com.palettee.portfolio.domain.QPortFolio.portFolio;
import static com.palettee.user.domain.QUser.user;

public class PortFolioRepositoryImpl implements PortFolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PortFolioRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Slice<PortFolioResponseDTO> PageFindAllPortfolio(Pageable pageable, MajorJobGroup majorJobGroup, MinorJobGroup minorJobGroup, String sort) {

        List<PortFolioResponseDTO> result = queryFactory
                .select(new QPortFolioResponseDTO(
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
                .limit(pageable.getPageSize()+1)
                .fetch();

        // 페이지 존재 여부를 나타내기 위해 하나 더 가져온걸 삭제
        boolean hasNext = false;


        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
            result.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private BooleanExpression majorJobGroupEquals(MajorJobGroup majorJobGroup) {
        return majorJobGroup != null ? user.majorJobGroup.eq(majorJobGroup) : null;
    }

    private BooleanExpression minorJobEquals(MinorJobGroup minorJobGroup) {
        return minorJobGroup != null ? user.minorJobGroup.eq(minorJobGroup) : null;
    }

    private OrderSpecifier<?> sortType(String type) {
        // PathBuilder로 해당 PortFolio 접근 가능
        PathBuilder<?> entityPath = new PathBuilder<>(PortFolio.class, "portFolio");

        if (type.equals("latest")) { // 최신순 정렬
            return new OrderSpecifier<>(Order.DESC, entityPath.get("createAt", LocalDateTime.class));  // 수정된 필드명
        } else if (type.equals("popularlity")) { // 인기순은 조회수를 기준으로
            return new OrderSpecifier<>(Order.DESC, entityPath.get("hits", Long.class));  // 수정된 필드
        }

        return null;
    }
}

