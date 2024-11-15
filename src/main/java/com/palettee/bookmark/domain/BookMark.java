package com.palettee.bookmark.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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


    public BookMark(Long bookMarkId, Long userId, Long portFolioId) {
        this.bookMarkId = bookMarkId;
        this.userId = userId;
        this.portFolioId = portFolioId;
    }
}
