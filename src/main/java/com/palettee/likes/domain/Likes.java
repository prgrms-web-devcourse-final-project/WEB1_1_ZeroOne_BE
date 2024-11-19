package com.palettee.likes.domain;


import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

// 완료

@Entity
@Getter
@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Likes(Long targetId, User user) {
        this.targetId = targetId;
        this.user = user;
        user.addLike(this);
    }
}
