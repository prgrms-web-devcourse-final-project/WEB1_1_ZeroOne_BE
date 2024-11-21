package com.palettee.global.security.jwt.controllers;

import com.palettee.global.security.dto.token.*;
import com.palettee.global.security.jwt.services.*;
import jakarta.servlet.http.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@SuppressWarnings("LoggingSimilarMessage")
public class TokenController {

    private final TokenService tokenService;

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
            HttpServletResponse resp) {

        TokenContainer result = tokenService.issueToken(temporaryToken);

        String accessToken = result.accessToken();
        String refreshToken = result.refreshToken();
        long expireSec = result.expiresInSeconds();

        // 응답 헤더, 쿠키에 토큰 넣기
        this.plantTokens(accessToken, refreshToken, resp);

        log.info("Access & refresh tokens were issued");

        return new TokenIssueResponse("토큰 발급이 완료되었습니다.", expireSec);
    }


    /**
     * 쿠키의 {@code Refresh} 토큰으로 {@code Access}, {@code Refresh} 토큰 발급하는 API
     * <p>
     * 이 때 {@code Redis} 에 저장된 {@code Refresh} 토큰이 없거나, 그것과 일치하는 {@code Refresh} 토큰이 제공되어야 발급
     */
    @PostMapping("/reissue")
    public TokenIssueResponse reissueToken(HttpServletRequest req, HttpServletResponse resp) {

        // cookie 에서 refresh 토큰 꺼내오기
        String refreshToken = getRefreshTokenFromCookie(req);

        TokenContainer result = tokenService.reissueToken(refreshToken);

        String accessToken = result.accessToken();
        String newRefreshToken = result.refreshToken();
        long expireSec = result.expiresInSeconds();

        // 응답 헤더, 쿠키에 토큰 넣기
        this.plantTokens(accessToken, newRefreshToken, resp);

        log.info("Access & refresh tokens were reissued");

        return new TokenIssueResponse("토큰 발급이 완료되었습니다.", expireSec);
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

        TokenContainer result = tokenService.issueTokenOnlyWithEmail(userEmail);

        String accessToken = result.accessToken();
        String refreshToken = result.refreshToken();
        long expireSec = result.expiresInSeconds();

        this.plantTokens(accessToken, refreshToken, resp);

        return new TokenIssueResponse("토큰 발급이 완료되었습니다.", expireSec);
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
     * 토큰 생성하고 응답, 쿠키에 넣어주는 메서드
     */
    private void plantTokens(
            String accessToken, String refreshToken,
            HttpServletResponse resp
    ) {

        Cookie refreshTokenCookie = tokenService.createRefreshTokenCookie(
                REFRESH_TOKEN_COOKIE_KEY, refreshToken
        );

        // 쿠키에 refresh 토큰 넣기
        resp.addCookie(refreshTokenCookie);
        log.debug("Set refresh token on cookie : {}", refreshTokenCookie);

        // 응답 헤더에 access 토큰 넣기
        resp.setHeader(ACCESS_TOKEN_HEADER, accessToken);
        log.debug("Set access token on response header : {}", accessToken);
    }
}
