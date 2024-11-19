package com.palettee.gathering;

import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gatheringId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Gathering(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        user.addGathering(this);
    }
}
