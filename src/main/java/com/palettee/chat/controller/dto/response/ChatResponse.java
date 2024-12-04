package com.palettee.chat.controller.dto.response;

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
    private String sendAt;

    public static ChatResponse toResponse(Long chatRoomId, User user, ChatRequest chatRequest) {
        if(chatRequest.imgUrls() == null && chatRequest.imgUrls().isEmpty()) {
            return new ChatResponse(
                    chatRoomId,
                    user.getEmail(),
                    user.getImageUrl(),
                    chatRequest.content(),
                    null,
                    TypeConverter.LocalDateTimeToString(LocalDateTime.now())
            );
        }

        return new ChatResponse(
                chatRoomId,
                user.getEmail(),
                user.getImageUrl(),
                chatRequest.content(),
                chatRequest.imgUrls(),
                TypeConverter.LocalDateTimeToString(LocalDateTime.now())
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
                TypeConverter.LocalDateTimeToString(chat.getSendAt())
        );
    }

    public Chat toChatEntity(User user, ChatRoom chatRoom) {
        return Chat.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .chatRoom(chatRoom)
                .content(content)
                .sendAt(TypeConverter.StringToLocalDateTime(sendAt))
                .build();
    }

    public List<ChatImage> toChatImageEntities(Chat chat) {
        return imgUrls.stream().map(chatImgUrl -> chatImgUrl.toEntityChatImage(chat)).toList();
    }
}
