package com.palettee.archive.domain;

import com.palettee.archive.controller.dto.request.ArchiveUpdateRequest;
import com.palettee.global.entity.BaseEntity;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long id;

    private String title;

    @Column(length = 2500)
    private String description;

    @Column(length = 2500)
    private String introduction;

    @Enumerated(EnumType.STRING)
    private ArchiveType type;

    private boolean canComment;

    private int hits;
    private int archiveOrder;

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
    public Archive(String title, String description, String introduction,
            ArchiveType type, boolean canComment, User user) {
        this.title = title;
        this.description = description;
        this.introduction = introduction;
        this.type = type;
        this.canComment = canComment;
        this.hits = 0;
        this.archiveOrder = 0;

        this.user = user;
        this.user.addArchive(this);
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

    public Archive update(ArchiveUpdateRequest req) {
        this.title = req.title();
        this.description = req.description();
        this.introduction = req.introduction();
        this.type = ArchiveType.findByInput(req.colorType());
        this.canComment = req.canComment();
        return this;
    }

    public void hit() {
        this.hits++;
    }

    public void setOrder() {
        this.archiveOrder = Integer.parseInt(String.valueOf(id));
    }

    public void updateOrder(Integer order) {
        this.archiveOrder = order;
    }

    public boolean isNotOpenComment() {
        return !this.canComment;
    }
}
