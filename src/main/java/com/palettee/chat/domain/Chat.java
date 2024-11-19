package com.palettee.chat.domain;

import com.palettee.chat_room.domain.*;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    private String content;


    @Builder
    public Chat(Long id, User user,
            ChatRoom chatRoom, String content) {
        this.id = id;
        this.user = user;
        this.chatRoom = chatRoom;
        this.content = content;
    }
}
