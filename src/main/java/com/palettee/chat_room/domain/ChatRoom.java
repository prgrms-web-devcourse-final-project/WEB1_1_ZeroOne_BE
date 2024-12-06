package com.palettee.chat_room.domain;

import com.palettee.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", nullable = false)
    private Long id;

    @Column(name = "chat_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatCategory chatCategory;

    @Builder
    public ChatRoom(ChatCategory chatCategory) {
        this.chatCategory = chatCategory;
    }
}
