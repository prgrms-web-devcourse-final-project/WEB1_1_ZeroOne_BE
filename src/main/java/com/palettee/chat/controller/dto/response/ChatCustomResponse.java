package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChatCustomResponse {
    private List<ChatResponse> chats;

    private boolean hasNext;

    private LocalDateTime lastSendAt;

    public static ChatCustomResponse toResponseFromEntity(List<Chat> chats, boolean hasNext,
                                                LocalDateTime nextChatTimeStamp) {
        return new ChatCustomResponse(
                chats.stream()
                        .map(ChatResponse::toResponseFromEntity)
                        .toList(),
                hasNext,
                nextChatTimeStamp
        );
    }

    public static ChatCustomResponse toResponseFromDto(List<ChatResponse> chats, boolean hasNext,
                                                LocalDateTime nextChatTimeStamp) {
        return new ChatCustomResponse(
                chats,
                hasNext,
                nextChatTimeStamp
        );
    }
}
