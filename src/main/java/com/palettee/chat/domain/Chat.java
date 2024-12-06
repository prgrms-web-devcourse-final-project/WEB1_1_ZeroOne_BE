package com.palettee.chat.domain;

import com.palettee.chat_room.domain.*;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {
    @Id
    @Column(name = "chat_id", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    private String content;

    private LocalDateTime sendAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    List<ChatImage> chatImages = new ArrayList<>();

    @Builder
    public Chat(String id,
            User user,
            ChatRoom chatRoom,
                String content,
                LocalDateTime sendAt) {
        this.id = id;
        this.user = user;
        this.chatRoom = chatRoom;
        this.content = content;
        this.sendAt = sendAt;
    }

    public void addChatImage(ChatImage chatImage) {
        this.chatImages.add(chatImage);
    }
}
