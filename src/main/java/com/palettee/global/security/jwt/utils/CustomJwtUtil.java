package com.palettee.global.security.jwt.utils;

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
        log.debug(s, e.getMessage());
        log.trace("Caused by: ", e);
    }

    private String removeBearer(String token) {
        return token != null && token.startsWith("Bearer ") ?
                token.substring(7) : token;
    }

    public boolean isValid(String token) {
        try {
            token = removeBearer(token);

            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userEmail = claims.get("userEmail", String.class);
            Date expiration = claims.getExpiration();

            if (userEmail == null || userEmail.isEmpty()) {
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

        } catch (ExpiredJwtException e) {
            logDebug("Token is expired : {}", e);

            return false;
        } catch (Exception e) {
            logDebug("Unexpected exception occurred while validating JWT : {}", e);

            return false;
        }
    }

    public boolean isExpired(String token) {
        token = removeBearer(token);

        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date(System.currentTimeMillis()));
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
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
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1_000 * expireTimeMin))
                .build();

        log.warn("Issued token at : {}", claims.getIssuedAt());
        log.warn("Token will be expired at : {}", claims.getExpiration());

        return Jwts.builder()
                .claims(claims)
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
            token = removeBearer(token);

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

    public UserRole getUserRole(String token)
            throws UnsupportedJwtException, ExpiredJwtException,
            IllegalArgumentException, RequiredTypeException,
            NullPointerException {

        try {
            token = removeBearer(token);

            String role = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);

            log.info("userRole = {}", role);

            if (role == null || role.isEmpty()) {
                throw new NullPointerException("No role exists on jwt payload");
            }

            return UserRole.valueOf(role);

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            logDebug("Invalid jwt given : {}", e);

            throw e;
        } catch (Exception e) {
            logDebug("Unexpected exception occurred while validating JWT : {}", e);

            throw new RuntimeException(e);
        }
    }
}
