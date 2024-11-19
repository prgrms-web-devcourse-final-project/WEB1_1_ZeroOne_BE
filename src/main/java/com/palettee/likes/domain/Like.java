package com.palettee.likes.domain;


import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    private Long userId;

    private Long portFolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Like(Long userId, Long portFolioId, User user) {
        this.userId = userId;
        this.portFolioId = portFolioId;
        this.user = user;
        user.addLike(this);
    }
}
