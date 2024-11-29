package com.palettee.global.exception;

import lombok.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 400 BAD_REQUEST : 잘못된 요청 */
    INVALID_TOKEN(400, "토큰이 유효하지 않습니다."),
    WRONG_SUB_PATH(400, "올바른 sub 경로가 아닙니다."),
    NO_EXIST_FILE(400, "파일이 존재하지 않습니다."),
    NOT_COMMENT_OWNER(400, "댓글 작성자가 아닙니다."),
    COMMENT_NOT_OPEN(400, "해당 아카이브는 댓글이 허용되지 않았습니다."),

    INVALID_DIVISION(400, "소속은 student, worker, etc 중 하나여야 합니다."),
    INVALID_JOB_GROUP(400, "직군이 올바르지 않습니다."),
    JOB_GROUP_MISMATCH(400, "주어진 소직군이 대직군에 포함되지 않습니다."),
    CATEGORY_NOT_FOUND(400, "해당 카테고리는 잘못된 요청입니다"),
    CHAT_CONTENT_NULL(400, "채팅 내용이 없습니다."),

    NOT_VALID_ALERT_TYPE(400, "존재하지 않는 알림 타입입니다."),
    NOT_MY_NOTIFICATION(400, "나의 알람이 아닙니다."),


    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),
    NOT_ACCESS(401, "접근 할 수 없는 유저입니다"),
    NO_TOKEN_EXISTS(401, "토큰이 존재하지 않습니다."),

    /* 403 UNAUTHORIZED : Resource 접근이 거부됨 */
    ROLE_MISMATCH(403, "권한이 부족합니다."),
    NOT_OWN_USER(403, "오직 본인만 사이트에 접근할 수 있습니다."),

    /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
    ARCHIVE_NOT_FOUND_EXCEPTION(404, "아카이브를 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(404, "해당하는 채팅방이 없습니다."),
    CHAT_USER_NOT_FOUND(404, "해당하는 채팅방 참여자가 없습니다."),
    PORT_FOLIO_NOT_FOUND(404, "해당 포트폴리오는 없습니다"),
    COMMENT_NOT_FOUND(404, "해당 댓글은 없습니다."),
  
    NO_USER_FOUND_VIA_TOKEN(404, "토큰으로 유저를 찾지 못했습니다."),
    USER_NOT_FOUND(404, "해당 유저를 찾지 못했습니다"),
    GATHERING_NOT_FOUND(404, "해당 게더링은 찾을 수 없습니다"),

    NOTIFICATION_NOT_FOUND(404, "해당 알림은 찾을 수 없습니다."),

    /* 415 Unsupported Media Type : 지원하지 않는 미디어 타입 */
    BAD_FILE_EXTENSION(415, "적절하지 않은 파일 확장자입니다."),

    /* 500 */
    INTERNAL_SERVER_ERROR(500, "서버 에러"),
    JSON_MAPPING_ERROR(500, "JSON 매핑에 실패하였습니다."),
    FILE_UPLOAD_FAIL(500,  "파일 업로드를 실패하였습니다.")
    ;

    private final int status;
    private final String reason;
}
