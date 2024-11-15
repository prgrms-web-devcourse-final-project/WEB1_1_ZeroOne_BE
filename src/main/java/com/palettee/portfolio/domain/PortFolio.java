package com.palettee.portfolio.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortFolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portFolio_id")
    private Long portfolioId;

    private String title;

    private String content;

    private int hits;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

    public PortFolio(String title, Long portfolioId, String content, int hits) {
        this.title = title;
        this.portfolioId = portfolioId;
        this.content = content;
        this.hits = hits;
    }
}
