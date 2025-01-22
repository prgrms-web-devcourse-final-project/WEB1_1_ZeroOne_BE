package com.palettee.global.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palettee.chat.controller.dto.request.ChatRequest;
import com.palettee.chat.domain.ChatUser;
import com.palettee.chat.exception.ChatUserNotFoundException;
import com.palettee.chat.repository.ChatRepository;
import com.palettee.chat.service.ChatUserService;
import com.palettee.chat_room.domain.ChatRoom;
import com.palettee.chat_room.service.ChatRoomService;
import com.palettee.global.handler.exception.*;
import com.palettee.global.security.jwt.exceptions.ExpiredTokenException;
import com.palettee.global.security.jwt.exceptions.InvalidTokenException;
import com.palettee.global.security.jwt.exceptions.RoleMismatchException;
import com.palettee.global.security.jwt.utils.JwtUtils;
import com.palettee.user.domain.User;
import com.palettee.user.domain.UserRole;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
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

    private static final String TOPIC_CHAT_ENDPOINT = "/sub/chat/";
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatUserService chatUserService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 프로토콜 메시지의 헤더와 관련된 정보를 쉽게 다룰 수 있도록 도와주는 유틸리티 객체 (헤더: 명령(COMMAND), 구독 정보, 세션 정보 등)
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");
        String destination = accessor.getDestination();

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            validateAuthorization(token);

            String email = jwtUtils.getEmailFromAccessToken(token);
            accessor.getSessionAttributes().put("email", email);
        }

        else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateDestination(destination);
            validateParticipation(accessor, destination);
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

        if(chatRequest.content() != null) {
            if(chatRequest.content().length() > 500) {
                log.error("채팅 내용 길이 Over 오류");
                throw ChatContentOverLength.EXCEPTION;
            }
        }

        if(chatRequest.imgUrls().size() > 3) {
            log.error("채팅 이미지 개수 Over 오류");
            throw ChatImageOverNumber.EXCEPTION;
        }
    }

    private void validateParticipation(StompHeaderAccessor accessor, String destination) {
        String email = (String) accessor.getSessionAttributes().get("email");
        User user = getUser(email);

        String chatRoomId = destination.substring(TOPIC_CHAT_ENDPOINT.length());
        ChatRoom chatRoom = chatRoomService.getChatRoom(Long.valueOf(chatRoomId));

        ChatUser chatUser = chatUserService.getChatUser(chatRoom.getId(), user.getId());
        if(chatUser.isDeleted()) {
           throw ChatUserNotFoundException.EXCEPTION;
        }
    }

    private User getUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
