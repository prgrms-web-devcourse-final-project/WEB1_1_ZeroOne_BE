package com.palettee.global.logging;

import java.util.*;
import lombok.*;

/**
 * 요청 {@code URI} 로 어떤 도메인 요청인 판별하기위한 편의용 {@code enum} 상수
 */
@RequiredArgsConstructor
public enum DomainType {
    ARCHIVE("/archive"), CHAT("/chat"),
    GATHERING("/gathering"), NOTIFICATION("/notification"),
    PORTFOLIO("/portFolio"), USER("/user"), OTHER("/other");

    private final String domainLogDir;

    public String getDomainLogDir() {
        // 맨 앞에 `/` 떼서 주기
        return domainLogDir.substring(1);
    }

    public static DomainType of(String uri) {

        // 특별 케이스들
        // 토큰 발급, 초기 정보 등록, 제보 request
        if (uri.startsWith("/token") || uri.startsWith("/profile") || uri.startsWith("/report")) {
            return USER;
        }

        // 채팅방, 웹소켓 request
        if (uri.startsWith("/chat-room") || uri.startsWith("/ws")) {
            return CHAT;
        }

        // 나머지
        return Arrays.stream(DomainType.values())
                .filter(d -> uri.startsWith(d.domainLogDir))
                .findFirst()
                .orElse(OTHER);
    }
}
