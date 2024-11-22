package com.palettee.archive.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String content;
    private String username;
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id")
    private Archive archive;

    @Builder
    public Comment(String content,
            String username, Long userId, Archive archive) {
        this.content = content;
        this.username = username;
        this.userId = userId;
        this.archive = archive;
        this.archive.addComment(this);
    }
}
