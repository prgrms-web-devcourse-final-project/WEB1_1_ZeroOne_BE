package com.palettee.chat.domain;

import com.palettee.chat_room.domain.*;
import com.palettee.global.entity.BaseEntity;
import com.palettee.user.domain.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseEntity {
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

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    List<ChatImage> chatImages = new ArrayList<>();

    @Builder
    public Chat(User user,
            ChatRoom chatRoom,
                String content) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.content = content;
    }

    public void addChatImage(ChatImage chatImage) {
        this.chatImages.add(chatImage);
    }
}
