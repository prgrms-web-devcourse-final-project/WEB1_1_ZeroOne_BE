package com.palettee.chat_room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", nullable = false)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

    @Column(name = "chat_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatCategory chatCategory;

    @Builder
    public ChatRoom(/**User user,**/ ChatCategory chatCategory) {
//        this.user = user;
        this.chatCategory = chatCategory;
    }
}
