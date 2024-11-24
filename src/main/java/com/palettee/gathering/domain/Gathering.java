package com.palettee.gathering.domain;

import com.palettee.global.entity.BaseEntity;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 완료

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gathering extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gathering_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Sort sort;

    @Enumerated(EnumType.STRING)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    private Contact contact; //연락 방법 -> 온라인 오프라인

    private String period; // 개발 기간

    private LocalDate deadLine; //마감일

    private int personnel; // 모집 인원

    @Enumerated(EnumType.STRING)
    private Position position; // 모집 포지션

    @Enumerated(EnumType.STRING)
    private Status status; // 현재 모집 상태

    @Column(name = "title", nullable = false, length = 50)
    private String title; // 모집 제목

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL)
    private List<GatheringTag> gatheringTagList;


    @Builder
    public Gathering(
            Sort sort,
            Subject subject,
            String period,
            Contact contact,
            LocalDate deadLine,
            int personnel,
            Position position,
            String title,
            String content,
            String url,
            User user,
            List<GatheringTag> gatheringTagList
    ) {
        this.sort = sort;
        this.subject = subject;
        this.period = period;
        this.contact = contact;
        this.deadLine = deadLine;
        this.url = url;
        this.personnel = personnel;
        this.position = position;
        this.status = Status.ONGOING;
        this.title = title;
        this.content = content;
        this.user = user;
       setGatheringTagList(gatheringTagList);
    }

    public void setGatheringTagList(List<GatheringTag> gatheringTagList) {
        for (GatheringTag gatheringTag : gatheringTagList) {
            gatheringTag.setGathering(this);
        }
        this.gatheringTagList = gatheringTagList;
    }

}
