package com.palettee.global.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.ErrorResponse;
import com.palettee.global.exception.PaletteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import static org.springframework.messaging.simp.stomp.StompCommand.ERROR;

/**
 * STOMP 메시지 처리 중 발생한 예외를 처리하고, 클라이언트에게 오류 메시지를 전송하는 기능
 */
@Component
@Slf4j
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    private final ObjectMapper objectMapper;

    public WebSocketErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable causeException = ex.getCause();
        if (causeException instanceof PaletteException) {
            return createMessageBytes((PaletteException) causeException);
        }

        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Message<byte[]> createMessageBytes(PaletteException paletteException) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(ERROR);
        ErrorCode errorCode = paletteException.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getStatus(), errorCode.getReason());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return MessageBuilder.createMessage(bytes, accessor.getMessageHeaders());
        } catch (JsonProcessingException e) {
            return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        }
    }
}
