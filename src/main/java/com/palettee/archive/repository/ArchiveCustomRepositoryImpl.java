package com.palettee.archive.repository;

import static com.palettee.archive.domain.QArchive.*;

import com.palettee.archive.domain.Archive;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ArchiveCustomRepositoryImpl implements ArchiveCustomRepository{

    private final JPAQueryFactory queryFactory;

    public ArchiveCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.queryFactory = jpaQueryFactory;
    }

    @Override
    public Slice<Archive> findAllArchiveWithCondition(String category, Pageable pageable) {
        JPAQuery<Long> query = queryFactory
                .select(archive.id)
                .from(archive)
                .where(makeCategoryCondition(category))
                .orderBy(makeOrderSpecifiers(archive, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        final Slice<Long> slice = toSlice(pageable, query.fetch());
        if (slice.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        final JPAQuery<Archive> jpaQuery = queryFactory.selectFrom(archive)
                .where(archive.id.in(slice.getContent()))
                .orderBy(makeOrderSpecifiers(archive, pageable));

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
                .orderBy(makeOrderSpecifiers(archive, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        final Slice<Long> slice = toSlice(pageable, query.fetch());
        if (slice.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        final JPAQuery<Archive> jpaQuery = queryFactory.selectFrom(archive)
                .where(archive.id.in(slice.getContent()))
                .orderBy(makeOrderSpecifiers(archive, pageable));

        return new SliceImpl<>(jpaQuery.fetch(), pageable, slice.hasNext());
    }

    private <T> OrderSpecifier[] makeOrderSpecifiers(final EntityPathBase<T> qClass, final Pageable pageable) {
        return pageable.getSort()
                .stream()
                .map(sort -> toOrderSpecifier(qClass, sort))
                .toList().toArray(OrderSpecifier[]::new);
    }

    private <T> OrderSpecifier toOrderSpecifier(final EntityPathBase<T> qClass, final Sort.Order sortOrder) {
        final PathBuilder<T> pathBuilder = new PathBuilder<>(qClass.getType(), qClass.getMetadata());
        return new OrderSpecifier(Order.DESC, pathBuilder.get(sortOrder.getProperty()));
    }

    public <T> Slice<T> toSlice(final Pageable pageable, final List<T> items) {
        if (items.size() > pageable.getPageSize()) {
            items.remove(items.size() - 1);
            return new SliceImpl<>(items, pageable, true);
        }
        return new SliceImpl<>(items, pageable, false);
    }

    private BooleanExpression makeCategoryCondition(String category) {
        if (category.equals("all")) {
            return null;
        }
        MajorJobGroup majorGroup = MajorJobGroup.findMajorGroup(category);
        if (majorGroup != null) {
            return archive.user.majorJobGroup.eq(majorGroup);
        }
        MinorJobGroup minorJobGroup = MinorJobGroup.findMinorJobGroup(category);
        return archive.user.minorJobGroup.eq(minorJobGroup);
    }
}
