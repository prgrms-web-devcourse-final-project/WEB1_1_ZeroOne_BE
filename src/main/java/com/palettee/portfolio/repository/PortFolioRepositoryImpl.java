package com.palettee.portfolio.repository;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.controller.dto.QPortFolioResponseDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import static com.palettee.portfolio.domain.QPortFolio.portFolio;
import static com.palettee.user.domain.QUser.user;

public class PortFolioRepositoryImpl implements PortFolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PortFolioRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    @Override
    public Slice<PortFolioResponseDTO> PageFinAllPortfolio(Pageable pageable, String sort, String jobGroup, String job) {

        queryFactory
                .select(new QPortFolioResponseDTO())
                .from(portFolio)
                .leftJoin(portFolio.user, user)
                .where()



    return null;
    }

//    private BooleanExpression jobGroupEquals(String jobGroup) {
//        return jobGroup != null  ? portFolio.jobGroup.eq(jobGroup)
//    }

//    private BooleanExpression jobEquals(String job) {
//        return job != null  ? portFolio.job.eq(job)
//    }
}
