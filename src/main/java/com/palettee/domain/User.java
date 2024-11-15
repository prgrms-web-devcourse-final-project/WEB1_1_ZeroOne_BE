package com.palettee.domain;

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

    @Column(name = "user_iamge_url")
    private String imageUrl;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_nickname")
    private String nickname;

    @Column(name = "brief_intro", length = 500)
    private String briefIntro;

    // one to many
    // 북마크?

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Gathering> gatherings = new ArrayList<>();

    @Builder
    public User(String email, String imageUrl, String name,
            String nickname, String briefIntro,
            List<Gathering> gatherings) {
        this.email = email;
        this.imageUrl = imageUrl;
        this.name = name;
        this.nickname = nickname;
        this.briefIntro = briefIntro;
        this.gatherings = gatherings;
    }
}
