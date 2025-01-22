package com.palettee.chat.domain;

import com.palettee.chat_room.domain.*;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Builder
    public ChatUser(ChatRoom chatRoom, User user, boolean isDeleted) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.isDeleted = isDeleted;
    }

    public void participation() {
        this.isDeleted = false;
    }

    public void leave() {
        this.isDeleted = true;
    }
}
