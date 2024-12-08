package com.palettee.global.configs;

import com.palettee.global.handler.StompHandler;
import com.palettee.global.handler.WebSocketErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
@Configuration
@RequiredArgsConstructor
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final WebSocketErrorHandler webSocketErrorHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트가 WebSocket 접속할 수 있는 엔드포인트
                .setAllowedOriginPatterns("*") // CORS 설정을 통해 모든 도메인에서 WebSocket 연결을 허용
                .withSockJS();
        registry.setErrorHandler(webSocketErrorHandler);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // "/sub"로 시작하는 구독 경로로 설정
        registry.setApplicationDestinationPrefixes("/pub"); // "/pub"로 시작하는 발행 경로로 설정
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 메시지가 @MessageMapping에 매핑되기 전에 이 해들러를 통해 추가 작업 실행
        registration.interceptors(stompHandler);
    }
}
