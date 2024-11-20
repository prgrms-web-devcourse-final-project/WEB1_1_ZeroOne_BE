package com.palettee.user.domain;

import com.palettee.archive.domain.*;
import com.palettee.gathering.*;
import com.palettee.likes.domain.*;
import com.palettee.portfolio.domain.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_image_url")
    private String imageUrl;

    @Column(name = "user_name")
    private String name;

    @Column(name = "brief_intro", length = 500)
    private String briefIntro;

    @Enumerated(EnumType.STRING)
    private MajorJobGroup majorJobGroup;

    @Enumerated(EnumType.STRING)
    private MinorJobGroup minorJobGroup;

    @Builder
    public User(String email, String imageUrl, String name, String briefIntro, MajorJobGroup majorJobGroup, MinorJobGroup minorJobGroup) {
        this.email = email;
        this.imageUrl = imageUrl;
        this.name = name;
        this.briefIntro = briefIntro;
        this.majorJobGroup = majorJobGroup;
        this.minorJobGroup = minorJobGroup;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Gathering> gatherings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<PortFolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<RelatedLink> relatedLinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Archive> archives = new ArrayList<>();


    public void addGathering(Gathering gathering) {
        this.gatherings.add(gathering);
    }

    public void addPortfolio(PortFolio portfolio) {
        this.portfolios.add(portfolio);
    }

    public void addLike(Likes likes) {
        this.likes.add(likes);
    }

    public void addRelatedLink(RelatedLink relatedLink) {
        this.relatedLinks.add(relatedLink);
    }

    public void addArchive(Archive archive) {
        this.archives.add(archive);
    }
}
