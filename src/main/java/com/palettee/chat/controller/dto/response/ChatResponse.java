package com.palettee.chat.controller.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.domain.Chat;
import com.palettee.user.domain.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatResponse {
    private Long chatRoomId;

    private Long userId;

    private String profileImg;

    private String content;

    private List<ChatImgUrl> imgUrls;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime sendAt;

    public static ChatResponse toResponse(Long chatRoomId, User user, ChatRequest chatRequest) {
        return new ChatResponse(
                chatRoomId,
                user.getId(),
                user.getImageUrl(),
                chatRequest.content(),
                chatRequest.imgUrls() == null || chatRequest.imgUrls().isEmpty() ?
                        null : chatRequest.imgUrls(),
                LocalDateTime.now()
        );
    }

    public static ChatResponse toResponseFromEntity(Chat chat) {
        return new ChatResponse(
                chat.getChatRoom().getId(),
                chat.getUser().getId(),
                chat.getUser().getImageUrl(),
                chat.getContent(),
                chat.getChatImages().stream()
                        .map(ChatImgUrl::toResponseFromEntity)
                        .toList(),
                chat.getSendAt()
        );
    }
}
