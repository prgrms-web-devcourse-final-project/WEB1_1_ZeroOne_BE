package com.palettee.global.security.dto.token;

public record TokenContainer(String accessToken, String refreshToken, long expiresInSeconds) {

}
