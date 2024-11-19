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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id")
    private Archive archive;

    @Builder
    public ArchiveImage(String imageUrl, Archive archive) {
        this.imageUrl = imageUrl;
        this.archive = archive;
        archive.addProjectImage(this);
    }
}
