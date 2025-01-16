package com.palettee.archive.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_image_id")
    private Long id;

    private String imageUrl;

    private Long archiveId;

    @Builder
    public ArchiveImage(String imageUrl, Long archiveId) {
        this.imageUrl = imageUrl;
        this.archiveId = archiveId;
    }
}
