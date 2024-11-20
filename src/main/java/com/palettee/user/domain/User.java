package com.palettee.user.domain;

import com.palettee.archive.domain.*;
import com.palettee.gathering.*;
import com.palettee.likes.domain.*;
import com.palettee.portfolio.domain.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_email", columnList = "user_email"),
        @Index(name = "idx_oauth_identity", columnList = "oauth_identity")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 사용자의 소셜 로그인 정보를 담는 column
     * <p>
     * > {@code google 12221}, {@code github 443312} 이런 형식
     */
    @Column(name = "oauth_identity", length = 50, unique = true)
    private String oauthIdentity;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_image_url")
    private String imageUrl;

    @Column(name = "user_name")
    private String name;

    @Column(name = "brief_intro", length = 500)
    private String briefIntro;


    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Gathering> gatherings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<PortFolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<RelatedLink> relatedLinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private final List<Archive> archives = new ArrayList<>();

    @Builder
    public User(
            String oauthIdentity, UserRole userRole,
            String email, String imageUrl,
            String name, String briefIntro
    ) {
        this.oauthIdentity = oauthIdentity;
        this.userRole = userRole;
        this.email = email;
        this.imageUrl = imageUrl;
        this.name = name;
        this.briefIntro = briefIntro;
    }

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
