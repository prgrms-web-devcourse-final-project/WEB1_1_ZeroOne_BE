package com.palettee.archive.repository;

import static com.palettee.archive.domain.QArchive.*;
import static com.palettee.user.domain.QUser.*;

import com.palettee.archive.domain.*;
import com.palettee.user.controller.dto.response.*;
import com.palettee.user.domain.*;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

@Repository
public class ArchiveCustomRepositoryImpl implements ArchiveCustomRepository{

    private final JPAQueryFactory queryFactory;

    public ArchiveCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public Slice<Archive> findAllArchiveWithCondition(String majorJObGroup, String minorJobGroup, String sort, Pageable pageable) {
        JPAQuery<Long> query = queryFactory
                .select(archive.id)
                .from(archive)
                .where(
                        majorJobGroupEquals(majorJObGroup),
                        minorJobEquals(minorJobGroup)
                )
                .orderBy(sortType(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        final Slice<Long> slice = toSlice(pageable, query.fetch());
        if (slice.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        final JPAQuery<Archive> jpaQuery = queryFactory.selectFrom(archive)
                .where(archive.id.in(slice.getContent()))
                .orderBy(sortType(sort));

        return new SliceImpl<>(jpaQuery.fetch(), pageable, slice.hasNext());
    }

    @Override
    public Slice<Archive> searchArchive(String searchKeyword, List<Long> ids, Pageable pageable) {
        JPAQuery<Long> query = queryFactory
                .select(archive.id)
                .from(archive)
                .where(
                        archive.title.like(searchKeyword)
                        .or(archive.description.like(searchKeyword))
                        .or(archive.id.in(ids))
                )
                .orderBy(sortType("latest"))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        final Slice<Long> slice = toSlice(pageable, query.fetch());
        if (slice.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        final JPAQuery<Archive> jpaQuery = queryFactory.selectFrom(archive)
                .where(archive.id.in(slice.getContent()))
                .orderBy(sortType("latest"));

        return new SliceImpl<>(jpaQuery.fetch(), pageable, slice.hasNext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetUserArchiveResponse findArchivesOnUserWithNoOffset(Long userId, int size,
            Long archiveOffset) {

        // id 내림차순 (최신순) 정렬
        List<Archive> searchResult = queryFactory
                .selectFrom(archive)
                .where(
                        archive.user.id.eq(userId), // 해당 유저 archive 중
                        archiveOffset != null ?     // (archive.id <= offset) 인 archive
                                archive.id.loe(archiveOffset) : null
                )
                .orderBy(archive.id.desc())
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

        return GetUserArchiveResponse.of(
                searchResult, hasNext, nextOffset
        );
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
        PathBuilder<?> entityPath = new PathBuilder<>(Archive.class, "archive");

        if (type.equals("latest")) {
            return new OrderSpecifier<>(Order.DESC, entityPath.get("createAt", LocalDateTime.class));
        } else if (type.equals("popularlity")) {
            return new OrderSpecifier<>(Order.DESC, entityPath.get("hits", Long.class));
        }

        return new OrderSpecifier<>(Order.ASC, entityPath.get("createAt", LocalDateTime.class));
    }

    public <T> Slice<T> toSlice(final Pageable pageable, final List<T> items) {
        if (items.size() > pageable.getPageSize()) {
            items.remove(items.size() - 1);
            return new SliceImpl<>(items, pageable, true);
        }
        return new SliceImpl<>(items, pageable, false);
    }

}
