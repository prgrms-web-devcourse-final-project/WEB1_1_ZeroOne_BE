package com.palettee.domain;

import jakarta.persistence.*;
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
    private String userEmail;

    @Column(name = "user_iamge_url")
    private String userImageUrl;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_nickname")
    private String userNickname;

    @Column(name = "brief_intro", length = 500)
    private String briefIntro;

    // one to many
    // 북마크?

    public User(String userEmail, String userImageUrl,
            String userName, String userNickname,
            String briefIntro) {
        this.userEmail = userEmail;
        this.userImageUrl = userImageUrl;
        this.userName = userName;
        this.userNickname = userNickname;
        this.briefIntro = briefIntro;
    }
}
