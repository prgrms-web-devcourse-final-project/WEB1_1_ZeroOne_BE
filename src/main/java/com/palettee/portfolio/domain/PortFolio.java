package com.palettee.portfolio.domain;

import com.palettee.global.entity.BaseEntity;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortFolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    private int hits;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private MajorJobGroup majorJobGroup;

    @Enumerated(EnumType.STRING)
    private MinorJobGroup minorJobGroup;

    @Builder
    public PortFolio(User user, String url,MajorJobGroup majorJobGroup, MinorJobGroup minorJobGroup) {
        this.hits = 0;
        this.user = user;
        this.url = url;
        this.minorJobGroup = minorJobGroup;
        this.majorJobGroup = majorJobGroup;
        user.addPortfolio(this);
    }

}
