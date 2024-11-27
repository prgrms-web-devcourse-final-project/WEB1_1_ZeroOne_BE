package com.palettee.chat.controller.dto.response;

import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private Long chatRoomId;
    private Long chatId;
    private String email;
    private String profileImg;
    private String content;
    private List<ChatImgUrlResponse> imgUrls;
    private LocalDateTime timestamp;

    public static ChatResponse toResponse(Long chatRoomId, Chat chat, List<ChatImage> chatImages) {
        if(chatImages == null) {
            return new ChatResponse(
                    chatRoomId,
                    chat.getId(),
                    chat.getUser().getEmail(),
                    chat.getUser().getImageUrl(),
                    chat.getContent(),
                    null,
                    chat.getCreateAt()
            );
        }

        return new ChatResponse(
                chatRoomId,
                chat.getId(),
                chat.getUser().getEmail(),
                chat.getUser().getImageUrl(),
                chat.getContent(),
                chatImages.stream()
                        .map(ChatImgUrlResponse::toResponse)
                        .toList(),
                chat.getCreateAt()
        );
    }
}
