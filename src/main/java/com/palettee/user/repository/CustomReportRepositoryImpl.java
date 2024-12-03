package com.palettee.user.repository;

import static com.palettee.user.domain.QReport.*;

import com.palettee.user.domain.*;
import com.querydsl.core.types.*;
import com.querydsl.jpa.impl.*;
import java.util.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

@Repository
@RequiredArgsConstructor
public class CustomReportRepositoryImpl implements
        CustomReportRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Slice<Report> findReportsWithConditions(
            Pageable pageable, String sort, String type, String include
    ) {
        sort = sort != null ? sort.toLowerCase() : "latest";
        type = type != null ? type.toLowerCase() : "all";
        include = include != null ? include.toLowerCase() : "all";

        JPAQuery<Long> query = jpaQueryFactory
                .select(report.id)
                .from(report)
                .where(
                        this.typeClauses(type),
                        this.includeClause(include)
                )
                .orderBy(this.sortType(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);

        final Slice<Long> slice = this.toSlice(pageable, query.fetch());
        if (slice.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        final JPAQuery<Report> jpaQuery = jpaQueryFactory.selectFrom(report)
                .where(report.id.in(slice.getContent()))
                .orderBy(this.sortType(sort));

        return new SliceImpl<>(jpaQuery.fetch(), pageable, slice.hasNext());
    }


    private Predicate typeClauses(String type) {
        return switch (type) {
            case "bug" -> report.reportType.eq(ReportType.BUG);
            case "enhance" -> report.reportType.eq(ReportType.ENHANCEMENT);
            case "other" -> report.reportType.eq(ReportType.OTHER);
            default -> null;
        };
    }

    private Predicate includeClause(String include) {
        return switch (include) {
            case "fixed" -> report.isFixed.isTrue();
            case "unfixed" -> report.isFixed.isFalse();
            default -> null;
        };
    }

    private OrderSpecifier<?> sortType(String sort) {
        return new OrderSpecifier<>(
                "oldest".equals(sort) ? Order.ASC : Order.DESC,
                report.id
        );
    }

    public <T> Slice<T> toSlice(final Pageable pageable, final List<T> items) {
        if (items.size() > pageable.getPageSize()) {
            items.remove(items.size() - 1);
            return new SliceImpl<>(items, pageable, true);
        }
        return new SliceImpl<>(items, pageable, false);
    }
}
