package com.palettee.portfolio.domain;

import com.palettee.portfolioUrl.domain.*;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortFolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    private String title;

    private String content;

    private int hits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "portfolio")
    private List<PortFolioUrl> portFolioUrlList;

    @Builder
    public PortFolio(String title, Long portfolioId, String content, User user) {
        this.title = title;
        this.portfolioId = portfolioId;
        this.content = content;
        this.hits = 0;
        this.user = user;
        user.addPortfolio(this);
    }
}
