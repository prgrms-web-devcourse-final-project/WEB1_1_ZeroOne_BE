package com.palettee.likes.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    public Like(Long userId, Long portFolioId) {
        this.userId = userId;
        this.portFolioId = portFolioId;
    }
}
