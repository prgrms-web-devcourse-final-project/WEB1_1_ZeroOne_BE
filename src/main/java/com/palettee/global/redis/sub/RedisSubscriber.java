package com.palettee.global.redis.sub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palettee.chat.controller.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    public void sendMessage(String publishMessage) {
        try {
            log.info("Received publishMessage: {}", publishMessage);
            ChatResponse roomMessage = objectMapper.readValue(publishMessage, ChatResponse.class);
            messagingTemplate.convertAndSend("/sub/chat/" + roomMessage.getChatRoomId(), roomMessage);
        } catch (Exception e) {
            log.error("subscriber error : " + e.getMessage());
        }
    }
}
