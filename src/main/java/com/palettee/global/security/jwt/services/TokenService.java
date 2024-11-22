package com.palettee.global.security.jwt.services;

import com.palettee.global.security.dto.token.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.servlet.http.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final RefreshTokenRedisService refreshTokenRedisService;

    /**
     * 임시 토큰으로 필수 토큰들을 발급
     *
     * @param temporaryToken 임시 토큰
     * @return {@code Access}, {@code Refresh} 토큰이 담긴 {@code DTO}
     * @throws NoTokenExistsException       토큰이 {@code null} 이거나 비어있을 시
     * @throws ExpiredTokenException        토큰 유효기간이 지났을 시
     * @throws InvalidTokenException        토큰이 유효하지 않을 시
     * @throws NoUserFoundViaTokenException 토큰은 유효하나 payload 의 {@code userEmail} 과 매칭되는 유저가 없을 시
     */
    public TokenContainer issueToken(String temporaryToken)
            throws NoTokenExistsException, ExpiredTokenException,
            InvalidTokenException, NoUserFoundViaTokenException {

        User user = validateTokenAndLinkToUser(
                temporaryToken,
                jwtUtils::isTemporaryTokenExpired,
                jwtUtils::isTemporaryTokenValid,
                jwtUtils::getEmailFromTemporaryToken
        );

        String accessToken = jwtUtils.createAccessToken(user);
        String refreshToken = jwtUtils.createRefreshToken(user);

        // 새로 발급된 refresh 를 redis 에 저장
        refreshTokenRedisService.storeRefreshToken(user, refreshToken,
                jwtUtils.getRefreshExpireMin());

        return new TokenContainer(accessToken, refreshToken,
                60 * jwtUtils.getAccessExpireMin());
    }


    /**
     * {@code Refresh} 토큰으로 필수 토큰들을 발급
     *
     * @return {@code Access}, {@code Refresh} 토큰이 담긴 {@code DTO}
     * @throws NoTokenExistsException       토큰이 {@code null} 이거나 비어있을 시
     * @throws ExpiredTokenException        토큰 유효기간이 지났을 시
     * @throws InvalidTokenException        토큰이 유효하지 않을 시
     * @throws NoUserFoundViaTokenException 토큰은 유효하나 payload 의 {@code userEmail} 과 매칭되는 유저가 없을 시
     */
    public TokenContainer reissueToken(String refreshToken)
            throws NoTokenExistsException, ExpiredTokenException,
            InvalidTokenException, NoUserFoundViaTokenException {

        User user = validateTokenAndLinkToUser(
                refreshToken,
                jwtUtils::isRefreshTokenExpired,
                jwtUtils::isRefreshTokenValid,
                jwtUtils::getEmailFromRefreshToken
        );

        String redisRefreshToken = refreshTokenRedisService.getRefreshToken(user).orElse(null);

        if (redisRefreshToken != null && !redisRefreshToken.equals(refreshToken)) {
            log.warn("Refresh token were valid, but fail to verifying with redis-stored token.");
            throw InvalidTokenException.EXCEPTION;
        }

        String accessToken = jwtUtils.createAccessToken(user);
        String newRefreshToken = jwtUtils.createRefreshToken(user);

        // 새로 발급된 refresh 를 redis 에 저장
        refreshTokenRedisService.storeRefreshToken(user, newRefreshToken,
                jwtUtils.getRefreshExpireMin());

        return new TokenContainer(accessToken, newRefreshToken,
                60 * jwtUtils.getAccessExpireMin());
    }


    /* !!! ------------------------------- !!! */
    // TODO : 개발 완료되면 삭제해야 함.
    public TokenContainer issueTokenOnlyWithEmail(String userEmail) {

        User user;

        if (userEmail == null || userEmail.isEmpty() ||
                (user = userRepo.findByEmail(userEmail).orElse(null)) == null) {
            log.error("주어진 이메일로 유저를 찾을 수 없습니다.");
            throw new IllegalArgumentException("주어진 이메일로 유저를 찾을 수 없습니다.");
        }

        String accessToken = jwtUtils.createAccessToken(user);
        String refreshToken = jwtUtils.createRefreshToken(user);

        refreshTokenRedisService.storeRefreshToken(user, refreshToken,
                jwtUtils.getRefreshExpireMin());

        return new TokenContainer(accessToken, refreshToken,
                60 * jwtUtils.getRefreshExpireMin());
    }
    /* !!! ------------------------------- !!! */


    /**
     * 토큰 검증을 진행하고 연관된 유저를 반환하는 메서드
     *
     * @param checkTokenExpiration 토큰 유효기간을 확인하는 function. 만료되면 {@code true} 를 뱉어야 함.
     * @param checkTokenValidation 토큰 유효성을 확인하는 function. 유효하면 {@code true} 를 뱉어야 함.
     * @param getEmailFromPayload  토큰 payload 에서 {@code userEmail} 값을 꺼내는 function.
     * @return 토큰과 연관된 {@code User}
     * @throws NoTokenExistsException       토큰이 {@code null} 이거나 비어있을 시
     * @throws ExpiredTokenException        토큰 유효기간이 지났을 시
     * @throws InvalidTokenException        토큰이 유효하지 않을 시
     * @throws NoUserFoundViaTokenException 토큰은 유효하나 payload 의 {@code userEmail} 과 매칭되는 유저가 없을 시
     */
    private User validateTokenAndLinkToUser(
            String token,
            Function<String, Boolean> checkTokenExpiration,
            Function<String, Boolean> checkTokenValidation,
            Function<String, String> getEmailFromPayload
    ) throws NoTokenExistsException, ExpiredTokenException,
            InvalidTokenException, NoUserFoundViaTokenException {

        // jwt 가 존재하지 않음
        if (token == null || token.isEmpty()) {
            log.error("Token is empty.");
            throw NoTokenExistsException.EXCEPTION;
        }

        // 유효기간이 지남
        if (checkTokenExpiration.apply(token)) {
            log.error("Token is expired.");
            throw ExpiredTokenException.EXCEPTION;
        }

        // jwt 가 유효하지 않음
        if (!checkTokenValidation.apply(token)) {
            log.error("Token is invalid.");
            throw InvalidTokenException.EXCEPTION;
        }

        String userEmail = getEmailFromPayload.apply(token);

        return userRepo.findByEmail(userEmail).orElseThrow(() -> {
            // jwt 는 유효하나 연관된 유저를 찾을 수 없음
            log.error("Cannot find user with email: {}", userEmail);
            return NoUserFoundViaTokenException.Exception;
        });
    }


    /**
     * {@code Refresh} 토큰이 담긴 쿠키를 생성
     *
     * @param key 쿠키 {@code key}
     */
    public Cookie createRefreshTokenCookie(String key, String token) {
        Cookie cookie = new Cookie(key, token);

        // 쿠키 시간은 ms
        cookie.setMaxAge(60 * 1_000 * (int) jwtUtils.getRefreshExpireMin());
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

}
