package com.palettee.global.security.dto.oauth;

public record OAuth2FailureResponse(int status, String reason, String timeStamp) {

}
