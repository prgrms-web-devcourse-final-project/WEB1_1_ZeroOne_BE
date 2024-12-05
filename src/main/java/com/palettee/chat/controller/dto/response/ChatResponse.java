package com.palettee.chat.controller.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.domain.Chat;
import com.palettee.chat.domain.ChatImage;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.global.redis.utils.TypeConverter;
import com.palettee.user.domain.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatResponse {
    private Long chatRoomId;

    private String email;

    private String profileImg;

    private String content;

    private List<ChatImgUrl> imgUrls;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime sendAt;

    public static ChatResponse toResponse(Long chatRoomId, User user, ChatRequest chatRequest) {
        if(chatRequest.imgUrls() == null && chatRequest.imgUrls().isEmpty()) {
            return new ChatResponse(
                    chatRoomId,
                    user.getEmail(),
                    user.getImageUrl(),
                    chatRequest.content(),
                    null,
                    LocalDateTime.now()
            );
        }

        return new ChatResponse(
                chatRoomId,
                user.getEmail(),
                user.getImageUrl(),
                chatRequest.content(),
                chatRequest.imgUrls(),
                LocalDateTime.now()
        );
    }

    public static ChatResponse toResponseFromEntity(Chat chat) {
        return new ChatResponse(
                chat.getChatRoom().getId(),
                chat.getUser().getEmail(),
                chat.getUser().getImageUrl(),
                chat.getContent(),
                chat.getChatImages().stream()
                        .map(chatImage -> ChatImgUrl.toResponseFromEntity(chatImage))
                        .toList(),
               chat.getSendAt()
        );
    }

    public Chat toChatEntity(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .chatRoom(chatRoom)
                .content(content)
                .sendAt(sendAt)
                .build();
    }

    public List<ChatImage> toChatImageEntities(Chat chat) {
        return imgUrls.stream().map(chatImgUrl -> chatImgUrl.toEntityChatImage(chat)).toList();
    }
}
