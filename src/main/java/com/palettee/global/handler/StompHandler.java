package com.palettee.global.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.global.handler.exception.ChatContentNullException;
import com.palettee.global.handler.exception.JSONMappingException;
import com.palettee.global.handler.exception.WrongSubPathException;
import com.palettee.global.security.jwt.exceptions.ExpiredTokenException;
import com.palettee.global.security.jwt.exceptions.InvalidTokenException;
import com.palettee.global.security.jwt.exceptions.RoleMismatchException;
import com.palettee.global.security.jwt.utils.JwtUtils;
import com.palettee.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private static final String TOPIC_CHAT_ENDPOINT = "/sub/chat";
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 프로토콜 메시지의 헤더와 관련된 정보를 쉽게 다룰 수 있도록 도와주는 유틸리티 객체 (헤더: 명령(COMMAND), 구독 정보, 세션 정보 등)
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            validateAuthorization(token);

            String email = jwtUtils.getEmailFromAccessToken(token);
            accessor.getSessionAttributes().put("email", email);
        }

        else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            validateDestination(destination);
            log.info("destination = {}", destination);
        }

        else if(StompCommand.SEND.equals(accessor.getCommand())) {
            validateUserRole(token);
            validateContent(message);
        }

        return message;
    }

    private void validateAuthorization(String token) {
        if(token == null || token.isEmpty()) {
            log.error("token is null");
            throw InvalidTokenException.EXCEPTION;
        }

        if(!jwtUtils.isAccessTokenValid(token)) {
            log.error("token is invalid");
            throw InvalidTokenException.EXCEPTION;
        }

        if(jwtUtils.isAccessTokenExpired(token)) {
            log.error("token is expired");
            throw ExpiredTokenException.EXCEPTION;
        }
    }

    private void validateDestination(String destination) {
        if (destination == null || destination.isEmpty()) {
            log.error("sub path is null");
            throw WrongSubPathException.EXCEPTION;
        }

        if (!destination.startsWith(TOPIC_CHAT_ENDPOINT)) {
            log.error("sub path is wrong");
            throw WrongSubPathException.EXCEPTION;
        }
    }

    private void validateUserRole(String token) {
        UserRole userRole = jwtUtils.getUserRoleFromAccessToken(token);
        log.info("userRole = {}", userRole);

        if (userRole.equals(UserRole.JUST_NEWBIE) || userRole.equals(UserRole.REAL_NEWBIE)) {
            log.error("Role is mismatch");
            throw RoleMismatchException.EXCEPTION;
        }
    }

    private void validateContent(Message<?> message) {
        String payload = new String((byte[]) message.getPayload());
        ChatRequest chatRequest = null;

        try {
            chatRequest = objectMapper.readValue(payload, ChatRequest.class);
            log.info("매핑된 ChatRequest = {}", chatRequest);
        } catch (JsonProcessingException e) {
            log.error("JSON 매핑 오류");
            throw JSONMappingException.EXCEPTION;
        }

        if(chatRequest == null) {
            log.error("JSON 매핑 오류");
            throw JSONMappingException.EXCEPTION;
        }

        if(chatRequest.content() == null && chatRequest.imgUrls() == null) {
            log.error("채팅 내용 null 오류");
            throw ChatContentNullException.EXCEPTION;
        }
    }
}
