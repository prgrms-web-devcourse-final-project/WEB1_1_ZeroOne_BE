package com.palettee.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    INVALID_TOKEN(401, "토큰이 유효하지 않습니다."), // 예시

    /* 403 UNAUTHORIZED : 인증되지 않은 사용자 */

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */
    CHAT_ROOM_NOT_FOUND(404, "해당하는 채팅방이 없습니다."),
    CHAT_USER_NOT_FOUND(404, "해당하는 채팅방 참여자가 없습니다."),

    /* 500 */
    INTERNAL_SERVER_ERROR(500,"서버 에러")
    ;

    private int status;
    private String reason;
}
