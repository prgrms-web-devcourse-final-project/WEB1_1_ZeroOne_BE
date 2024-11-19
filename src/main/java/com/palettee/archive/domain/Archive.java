package com.palettee.archive.domain;

import com.palettee.user.domain.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ArchiveType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "archive", cascade = CascadeType.REMOVE)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "archive", cascade = CascadeType.REMOVE)
    private List<ArchiveImage> archiveImages = new ArrayList<>();

    @OneToMany(mappedBy = "archive", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Archive(Long id, String title, String description,
            ArchiveType type, User user,
            List<Tag> tags, List<ArchiveImage> archiveImages,
            List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;

        this.user = user;
        this.user.addArchive(this);

        this.tags = tags;
        this.archiveImages = archiveImages;
        this.comments = comments;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void addProjectImage(ArchiveImage archiveImage) {
        this.archiveImages.add(archiveImage);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
