package com.palettee.global.security.jwt.controllers;

import com.palettee.global.redis.*;
import com.palettee.global.security.dto.token.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class TokenController {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final RedisService redisService;

    private static final String REFRESH_TOKEN_COOKIE_KEY = "refresh_token";
    private static final String ACCESS_TOKEN_HEADER = "Authorization";


    /**
     * 요청의 {@code Query param} 으로 주어진 {@code 임시 토큰} 으로 {@code Access}, {@code Refresh} 토큰을 발급하는 API
     *
     * @param temporaryToken 유효기간이 짧은 임시 토큰
     */
    @GetMapping("/issue")
    public TokenIssueResponse issueToken(
            @RequestParam(value = "token", required = false) String temporaryToken,
            HttpServletResponse resp
    ) throws NoTokenExistsException, InvalidTokenException, ExpiredTokenException {

        // 토큰 검증 + 토큰 userEmail 로 유저 찾기
        // 검증 실패 or 유저 못찾으면 메서드 속에서 throw 됨.
        User user = validateTokenToUser(
                temporaryToken,
                jwtUtils::isTemporaryTokenExpired,
                jwtUtils::isTemporaryTokenValid,
                jwtUtils::getEmailFromTemporaryToken
        );

        log.info("Token were valid & available to find user via email");

        // jwt 유효하고 유저도 찾을 수 있음 --> happy flow
        String accessToken = jwtUtils.createAccessToken(user);
        String refreshToken = jwtUtils.createRefreshToken(user);

        // 응답 헤더, 쿠키에 토큰 넣기
        this.plantTokens(accessToken, refreshToken, resp);

        // redis 에 발급한 refresh 토큰 저장하기
        redisService.storeRefreshToken(user, refreshToken, jwtUtils.getRefreshExpireMin());

        return new TokenIssueResponse(
                "토큰 발급이 완료되었습니다.",
                60 * jwtUtils.getAccessExpireMin()
        );
    }


    /**
     * 쿠키의 {@code Refresh} 토큰으로 {@code Access}, {@code Refresh} 토큰 발급하는 API
     * <p>
     * 이 때 {@code Redis} 에 저장된 {@code Refresh} 토큰이 없거나, 그것과 일치하는 {@code Refresh} 토큰이 제공되어야 발급
     */
    @PostMapping("/reissue")
    public TokenIssueResponse reissueToken(HttpServletRequest req, HttpServletResponse resp)
            throws NoTokenExistsException, InvalidTokenException, ExpiredTokenException {

        // cookie 에서 refresh 토큰 꺼내오기
        String refreshToken = getRefreshTokenFromCookie(req);

        // 토큰 검증 + 토큰 userEmail 로 유저 찾기
        // 검증 실패 or 유저 못찾으면 메서드 속에서 throw 됨.
        User user = validateTokenToUser(
                refreshToken,
                jwtUtils::isRefreshTokenExpired,
                jwtUtils::isRefreshTokenValid,
                jwtUtils::getEmailFromRefreshToken
        );

        log.info("Token were valid & available to find user via email");

        // redis 에서도 꺼내 맞는지 확인해야 함.
        String refreshTokenFromRedis = redisService.getRefreshToken(user).orElse(null);

        // 주어진 토큰이 redis 의 것과 맞지 않음.
        if (refreshTokenFromRedis != null && !refreshTokenFromRedis.equals(refreshToken)) {
            log.warn("Refresh token were valid, but fail to verifying with redis-stored token.");
            log.trace("Given refresh token : {}", refreshToken);
            log.trace("Redis given refresh token : {}", refreshTokenFromRedis);

            throw InvalidTokenException.EXCEPTION;
        }

        // redis 에 refresh 토큰이 없었거나, 있었는데 주어진 값과 동일하면
        // --> happy flow

        String accessToken = jwtUtils.createAccessToken(user);
        String newRefreshToken = jwtUtils.createRefreshToken(user);

        // 응답 헤더, 쿠키에 토큰 넣기
        this.plantTokens(accessToken, newRefreshToken, resp);

        // redis 에 새로운 refresh 토큰 저장하기
        redisService.storeRefreshToken(user, newRefreshToken, jwtUtils.getRefreshExpireMin());

        log.info("Access & refresh tokens were reissued");

        return new TokenIssueResponse(
                "토큰 발급이 완료되었습니다.",
                60 * jwtUtils.getAccessExpireMin()
        );
    }


    /**
     * 개발 중 원활한 테스트를 위해 {@code userEmail} 로 토큰 발급해주는 API
     *
     * @param userEmail DB 에 존재하는 어느 유저의 email
     */
    // TODO : 개발 완료되면 삭제해야 함.
    @PostMapping("/test-issue")
    public TokenIssueResponse testIssueToken(
            @RequestParam(value = "userEmail", required = false) String userEmail,
            HttpServletResponse resp
    ) {

        if (userEmail == null || userEmail.isEmpty()) {
            log.error("userEmail 이 비어있습니다.");
            throw new NullPointerException("userEmail 이 비어있습니다.");
        }

        User user = userRepo.findByEmail(userEmail).orElse(null);

        if (user == null) {
            log.error("주어진 email 로 유저를 찾을 수 없습니다.");
            throw new NullPointerException("주어진 email 로 유저를 찾을 수 없습니다.");
        }

        String accessToken = jwtUtils.createAccessToken(user);
        String refreshToken = jwtUtils.createRefreshToken(user);

        plantTokens(accessToken, refreshToken, resp);

        redisService.storeRefreshToken(user, refreshToken, jwtUtils.getRefreshExpireMin());

        return new TokenIssueResponse(
                "토큰 발급이 완료되었습니다.",
                60 * jwtUtils.getAccessExpireMin()
        );
    }


    /**
     * 쿠키에서 {@code Refresh} 토큰 꺼내오기
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        return Arrays.stream(cookies).parallel()
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_COOKIE_KEY))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }


    /**
     * 토큰 검증하고 payload 의 {@code userEmail} 로 유저 찾아서 반환하는 편의용 메서드
     *
     * @param token               jwt 토큰
     * @param checkExpiration     토큰 유효기간이 만료됐는지 확인하는 function
     * @param validateToken       토큰이 유효한지 확인하는 function
     * @param getEmailFromPayload 토큰에서 {@code userEmail} payload 를 꺼내는 function
     * @return 토큰으로 확실히 검증된 {@code User}
     */
    private User validateTokenToUser(String token,
            Function<String, Boolean> checkExpiration,
            Function<String, Boolean> validateToken,
            Function<String, String> getEmailFromPayload)
            throws NoTokenExistsException, InvalidTokenException, NoUserFoundViaTokenException {

        // jwt 가 존재하지 않음.
        if (token == null || token.isEmpty()) {
            throw NoTokenExistsException.EXCEPTION;
        }

        // 유효기간이 지남
        if (checkExpiration.apply(token)) {
            throw ExpiredTokenException.EXCEPTION;
        }

        // jwt 가 유효하지 않음.
        if (!validateToken.apply(token)) {
            throw InvalidTokenException.EXCEPTION;
        }

        User user = userRepo.findByEmail(getEmailFromPayload.apply(token))
                .orElse(null);

        // jwt-userEmail 로 유저를 찾을 수 없음.
        if (user == null) {
            throw NoUserFoundViaTokenException.Exception;
        }

        return user;
    }


    /**
     * 토큰 생성하고 응답, 쿠키에 넣어주는 메서드
     */
    private void plantTokens(
            String accessToken, String refreshToken,
            HttpServletResponse resp
    ) {
        Cookie refreshTokenCookie = createCookie(REFRESH_TOKEN_COOKIE_KEY,
                refreshToken, jwtUtils.getRefreshExpireMin());

        // 쿠키에 refresh 토큰 넣기
        resp.addCookie(refreshTokenCookie);
        log.debug("Set refresh token on cookie : {}", refreshTokenCookie);

        // 응답 헤더에 access 토큰 넣기
        resp.setHeader(ACCESS_TOKEN_HEADER, accessToken);
        log.debug("Set access token on response header : {}", accessToken);
    }


    @SuppressWarnings("SameParameterValue")
    private Cookie createCookie(String key, String value, long expireMin) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 1_000 * (int) expireMin);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
