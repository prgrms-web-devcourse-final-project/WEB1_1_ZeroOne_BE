package com.palettee.global.security.jwt.utils;

import com.palettee.user.domain.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

/**
 * Jwt 토큰과 관련된 {@code Component}
 *
 * @see CustomJwtUtil
 */
@Slf4j
@Component
public class JwtUtils {

    private final CustomJwtUtil temporaryJwtUtil;
    private final CustomJwtUtil accessJwtUtil;
    private final CustomJwtUtil refreshJwtUtil;

    private final @Getter long tempoExpireMin;
    private final @Getter long accessExpireMin;
    private final @Getter long refreshExpireMin;

    public JwtUtils(
            @Value("${jwt.temporary.secret}") String tempoSecret,
            @Value("${jwt.temporary.expire-time-min}") long tempoExpireMin,
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.access.expire-time-min}") long accessExpireMin,
            @Value("${jwt.refresh.secret}") String refreshSecret,
            @Value("${jwt.refresh.expire-time-min}") long refreshExpireMin
    ) {
        this.temporaryJwtUtil = new CustomJwtUtil(tempoSecret);
        this.accessJwtUtil = new CustomJwtUtil(accessSecret);
        this.refreshJwtUtil = new CustomJwtUtil(refreshSecret);

        this.tempoExpireMin = tempoExpireMin;
        this.accessExpireMin = accessExpireMin;
        this.refreshExpireMin = refreshExpireMin;
    }

    // 임시 토큰과 관련된 utils
    public boolean isTemporaryTokenValid(String temporaryToken) {
        return temporaryJwtUtil.isValid(temporaryToken);
    }

    public boolean isTemporaryTokenExpired(String temporaryToken) {
        return temporaryJwtUtil.isExpired(temporaryToken);
    }

    public String createTemporaryToken(User user) {
        return temporaryJwtUtil.createToken(user, tempoExpireMin);
    }

    /**
     * @see CustomJwtUtil#getEmail(String)
     */
    public String getEmailFromTemporaryToken(String temporaryToken) {
        return temporaryJwtUtil.getEmail(temporaryToken);
    }

    // access 토큰과 관련된 utils
    public boolean isAccessTokenValid(String accessToken) {
        return accessJwtUtil.isValid(accessToken);
    }

    public boolean isAccessTokenExpired(String accessToken) {
        return accessJwtUtil.isExpired(accessToken);
    }

    public String createAccessToken(User user) {
        return accessJwtUtil.createToken(user, accessExpireMin);
    }

    /**
     * @see CustomJwtUtil#getEmail(String)
     */
    public String getEmailFromAccessToken(String accessToken) {
        return accessJwtUtil.getEmail(accessToken);
    }

    // refresh 토큰과 관련된 utils
    public boolean isRefreshTokenValid(String refreshToken) {
        return refreshJwtUtil.isValid(refreshToken);
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        return refreshJwtUtil.isExpired(refreshToken);
    }

    public String createRefreshToken(User user) {
        return refreshJwtUtil.createToken(user, refreshExpireMin);
    }

    /**
     * @see CustomJwtUtil#getEmail(String)
     */
    public String getEmailFromRefreshToken(String refreshToken) {
        return refreshJwtUtil.getEmail(refreshToken);
    }
}
