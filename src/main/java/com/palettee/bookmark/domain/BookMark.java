package com.palettee.bookmark.domain;

import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookMark_id")
    private Long bookMarkId;

    private Long userId;

    private Long portFolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public BookMark(Long userId, Long portFolioId, User user) {
        this.userId = userId;
        this.portFolioId = portFolioId;
        this.user = user;
        user.addBookMark(this);
    }
}
