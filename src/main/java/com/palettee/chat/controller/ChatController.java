package com.palettee.chat.controller;

import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.controller.dto.request.ChatImgRequest;
import com.palettee.chat.controller.dto.response.ChatImgResponse;
import com.palettee.chat.controller.dto.response.ChatResponse;
import com.palettee.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat/{chatRoomId}")
    public void chatting(
            @DestinationVariable(value = "chatRoomId") Long chatRoomId,
            @Payload ChatRequest chatRequest,
            SimpMessageHeaderAccessor accessor
    ) {

        String email = (String) accessor.getSessionAttributes().get("email");
        ChatResponse chatResponse = chatService.saveChat(email, chatRoomId, chatRequest);
        simpMessagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, chatResponse);
    }

    @MessageMapping("/chat-image/{chatRoomId}")
    public void imageChatting(
            @DestinationVariable(value = "chatRoomId") Long chatRoomId,
            @Payload ChatImgRequest chatImgRequest,
            SimpMessageHeaderAccessor accessor
    ) {

        String email = (String) accessor.getSessionAttributes().get("email");
        ChatImgResponse chatImgResponse = chatService.saveImageMessage(email, chatRoomId, chatImgRequest);
        simpMessagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, chatImgResponse);
    }
}
