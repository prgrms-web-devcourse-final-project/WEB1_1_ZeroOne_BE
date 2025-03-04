package com.palettee.chat.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    private String imageUrl;

    @Builder
    public ChatImage(Chat chat, String imageUrl) {
        this.chat = chat;
        this.imageUrl = imageUrl;
        chat.addChatImage(this);
    }
}
