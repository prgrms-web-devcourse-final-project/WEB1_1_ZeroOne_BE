package com.palettee.portfolio.domain;

import com.palettee.global.entity.BaseEntity;
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

    @Builder
    public PortFolio(User user, String url) {
        this.hits = 0;
        this.user = user;
        this.url = url;
        user.addPortfolio(this);
    }

    public void incrementHits(){
        this.hits++;
    }
}
