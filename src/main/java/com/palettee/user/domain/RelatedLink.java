package com.palettee.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelatedLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relatedLinkId;

    @Column(nullable = false, length = 100)
    private String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public RelatedLink(String link, User user) {
        this.link = link;
        this.user = user;
        user.addRelatedLink(this);
    }
}
