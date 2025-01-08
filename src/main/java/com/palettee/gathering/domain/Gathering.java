package com.palettee.gathering.domain;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.global.entity.BaseEntity;
import com.palettee.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private Status status; // 현재 모집 상태

    @Column(name = "title", nullable = false, length = 50)
    private String title; // 모집 제목

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int hits;

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GatheringTag> gatheringTagList = new ArrayList<>();

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GatheringImage> gatheringImages= new ArrayList<>();

    @OneToMany(mappedBy = "gathering", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    @Builder
    public Gathering(
            Sort sort,
            Subject subject,
            String period,
            Contact contact,
            LocalDate deadLine,
            int personnel,
            String title,
            String content,
            String url,
            User user,
            List<Position> positions,
            List<GatheringTag> gatheringTagList,
            List<GatheringImage> gatheringImages
    ) {
        this.sort = sort;
        this.subject = subject;
        this.period = period;
        this.contact = contact;
        this.deadLine = deadLine;
        this.url = url;
        this.personnel = personnel;
        this.status = Status.ONGOING;
        this.title = title;
        this.content = content;
        this.user = user;
        this.hits = 0;
        user.addGathering(this);
       setGatheringTagList(gatheringTagList);
       setGatheringImages(gatheringImages);
       setPositions(positions);
    }

    public void setGatheringTagList(List<GatheringTag> gatheringTagList) {
        if(gatheringTagList != null && !gatheringTagList.isEmpty()){
            for (GatheringTag gatheringTag : gatheringTagList) {
                gatheringTag.setGathering(this);
                this.gatheringTagList.add(gatheringTag);
            }
        }

    }

    public void setGatheringImages(List<GatheringImage> gatheringImages){
        if(gatheringImages != null && !gatheringImages.isEmpty()){
            System.out.println(gatheringImages.size());
            for(GatheringImage gatheringImage : gatheringImages){
                gatheringImage.setGathering(this);
                this.gatheringImages.add(gatheringImage);
            }
        }

    }

    public void setPositions(List<Position> positions) {
        if(positions != null && !positions.isEmpty()){
            for(Position position : positions){
                position.setGathering(this);
                this.positions.add(position);
            }
        }
    }

    public void updateGathering(GatheringCommonRequest gathering){
        this.sort = Sort.findSort(gathering.sort());
        this.subject = Subject.finSubject(gathering.subject());
        this.contact = Contact.findContact(gathering.contact());
        this.period = gathering.period();
        this.deadLine = GatheringCommonRequest.getDeadLineLocalDate(gathering.deadLine());
        this.personnel = gathering.personnel();
        this.title = gathering.title();
        this.content = gathering.content();
        this.url = gathering.url();
        updateGatheringPosition(gathering);
        updateGatheringTag(gathering);
        updateGatheringImages(gathering);
    }


    // 이미지 태그 있을때만 교체
    private void updateGatheringTag(GatheringCommonRequest gathering) {
        if(gathering.gatheringTag()!= null && !gathering.gatheringTag().isEmpty()){
            this.gatheringTagList.clear();

            List<GatheringTag> gatheringTag = GatheringCommonRequest.getGatheringTag(gathering.gatheringTag());
            setGatheringTagList(gatheringTag);
        }
    }
    // 이미지가 있을때만 교체
    private void updateGatheringImages(GatheringCommonRequest gathering){
        if(gathering.gatheringImages()!=null && !gathering.gatheringImages().isEmpty()){
            this.gatheringImages.clear();
            List<GatheringImage> gatheringImage = GatheringCommonRequest.getGatheringImage(gathering.gatheringImages());
            setGatheringImages(gatheringImage);
        }
    }

    // 포지션이 있을때만 교체
    private void updateGatheringPosition(GatheringCommonRequest gathering){
        if(gathering.positions() != null && !gathering.positions().isEmpty()){
            this.positions.clear();
            List<Position> position = GatheringCommonRequest.getPosition(gathering.positions());
            setPositions(position);
        }
    }

    public void updateStatusComplete(){
       this.status = Status.COMPLETE;
    }


}
