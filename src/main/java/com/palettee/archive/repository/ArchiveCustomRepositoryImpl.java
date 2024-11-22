package com.palettee.archive.repository;

import static com.palettee.archive.domain.QArchive.*;
import static com.palettee.user.domain.QUser.user;

import com.palettee.archive.domain.Archive;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

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
