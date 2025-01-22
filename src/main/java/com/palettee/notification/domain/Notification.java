package com.palettee.notification.domain;

import com.palettee.global.entity.BaseEntity;
import com.palettee.likes.domain.LikeType;
import com.palettee.notification.exception.NotMyNotification;
import com.palettee.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    private Long targetId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    private Boolean isRead;

    private Long chatRoomId;

    private Long userId;

    private Long contentId;

    @Enumerated(EnumType.STRING)
    private LikeType likeType;

    @Builder
    public Notification(Long targetId, String title, String content, AlertType type, Long chatRoomId, Long userId,
                        Long contentId, LikeType likeType) {
        this.targetId = targetId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = false;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.contentId = contentId;
        this.likeType = likeType;
    }

    public void read(User user) {
        if (!user.getId().equals(targetId)) {
            throw NotMyNotification.EXCEPTION;
        }
        this.isRead = true;
    }
}
