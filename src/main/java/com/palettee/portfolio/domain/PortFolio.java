package com.palettee.portfolio.domain;

import com.palettee.portfolioUrl.domain.PortFolioUrl;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @OneToMany(mappedBy = "portfolio")
    private List<PortFolioUrl> portFolioUrlList;

    @Builder
    public PortFolio(String title, Long portfolioId, String content) {
        this.title = title;
        this.portfolioId = portfolioId;
        this.content = content;
        this.hits = 0;
    }
}
