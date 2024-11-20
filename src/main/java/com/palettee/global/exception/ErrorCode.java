package com.palettee.global.exception;

import lombok.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    INVALID_TOKEN(400, "토큰이 유효하지 않습니다."), // 예시

    /* token 관련 error code */
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),
    NO_TOKEN_EXISTS(401, "토큰이 존재하지 않습니다."),
    ROLE_MISMATCH(403, "권한이 부족합니다."),
    NO_USER_FOUND_VIA_TOKEN(404, "토큰으로 유저를 찾지 못했습니다."),

    /* 403 UNAUTHORIZED : 인증되지 않은 사용자 */

    /* 404 NOT_FOUND : Resource를 찾을 수 없음 */

    /* 500 */
    INTERNAL_SERVER_ERROR(500, "서버 에러");

    private final int status;
    private final String reason;
}
