package com.palettee.chat.controller;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.service.ChatService;
import com.palettee.global.redis.pub.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;
    private final ChannelTopic channelTopic;

    @MessageMapping("/chat/{chatRoomId}")
    public void chatting(
            @DestinationVariable(value = "chatRoomId") Long chatRoomId,
            @Payload ChatRequest chatRequest,
            SimpMessageHeaderAccessor accessor
    ) {

        String email = (String) accessor.getSessionAttributes().get("email");
        ChatResponse response = chatService.saveChat(email, chatRoomId, chatRequest);
        redisPublisher.publish(channelTopic, response);
    }
}
