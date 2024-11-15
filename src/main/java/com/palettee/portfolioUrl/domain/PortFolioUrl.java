package com.palettee.portfolioUrl.domain;


import com.palettee.portfolio.domain.PortFolio;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortFolioUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portFolioUrl_id")
    private Long portfolioUrlId;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portFolio_id")
    private PortFolio portfolio;


    public PortFolioUrl(String url, PortFolio portfolio) {
        this.url = url;
        this.portfolio = portfolio;
        addPortfolio(portfolio);
    }

    public void addPortfolio(PortFolio portfolio) {
        this.portfolio = portfolio;
        portfolio.getPortFolioUrlList().add(this);
    }
}
