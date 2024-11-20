package com.palettee.global.security.jwt;

import com.palettee.user.domain.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.*;
import java.util.*;
import javax.crypto.*;
import lombok.extern.slf4j.*;

/**
 * Temporary, Access, Refresh 토큰 발급, 확인을 위한 클래스
 */
@Slf4j
final class CustomJwtUtil {

    private final SecretKey secretKey;

    public CustomJwtUtil(String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }

    private static void logDebug(String s, Exception e) {
        log.debug(s, e);
    }

    public boolean isValid(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.get("email", String.class);
            Date expiration = claims.getExpiration();

            if (email == null || email.isEmpty()) {
                logDebug("Cannot find userEmail claims in jwt {}", new NullPointerException());
                return false;
            }

            if (expiration.before(new Date(System.currentTimeMillis()))) {
                logDebug("Token were expired", new IllegalStateException());
                return false;
            }

            return true;

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            logDebug("Invalid jwt given : {}", e);

            return false;
        } catch (Exception e) {
            logDebug("Unexpected exception occurred while validating JWT : {}", e);

            return false;
        }
    }

    public boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    /**
     * 토큰 발급
     *
     * @param user          사용자 정보
     * @param expireTimeMin 만료 시간 (분)
     * @return {@code String} 토큰
     */
    public String createToken(User user, long expireTimeMin) {

        Claims claims = Jwts.claims()
                .add("userEmail", user.getEmail())
                .add("role", user.getUserRole())
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1_000 * expireTimeMin))
                .signWith(this.secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 토큰에서 유저 이메일 parse 하는 메서드
     *
     * @throws UnsupportedJwtException  토큰키가 맞지 않을 때
     * @throws ExpiredJwtException      토큰이 만료되었을 때
     * @throws IllegalArgumentException 토큰이 {@code null} 이거나 이상한 공백이 있을 때
     * @throws RequiredTypeException    파싱시 payload 속 {@code userEmail} 이 문자열이 아닐때
     * @throws NullPointerException     payload 에 {@code userEmail} claims 가 없을 때
     */
    public String getEmail(String token)
            throws UnsupportedJwtException, ExpiredJwtException,
            IllegalArgumentException, RequiredTypeException,
            NullPointerException {

        try {
            String email = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userEmail", String.class);

            if (email == null || email.isEmpty()) {
                throw new NullPointerException("No email exists on jwt payload");
            }

            return email;

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            logDebug("Invalid jwt given : {}", e);

            throw e;
        } catch (Exception e) {
            logDebug("Unexpected exception occurred while validating JWT : {}", e);

            throw new RuntimeException(e);
        }
    }
}
