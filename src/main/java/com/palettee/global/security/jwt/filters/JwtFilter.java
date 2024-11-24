package com.palettee.global.security.jwt.filters;

import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.global.security.oauth.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.filter.*;

/**
 * 요청 헤더의 Access 토큰으로 SecurityContext 에 유저 정보 넣어주기 위한 jwt filter
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final BypassUrlHolder bypassHolder;

    private static final String ACCESS_TOKEN_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 현재 요청이 권한 필요없는 요청이면 jwt 동작 안하기
        if (isByPassable(request)) {
            logBypass(request);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Request is not a candidate for bypass.");

        // 만약 jwt 파싱시 에러 터져도 그냥 넘어가도 되는지 확인
        boolean conditionalAuthRequired = conditionalAuthRequired(request);

        // request header 에서 jwt 꺼내기
        String token = request.getHeader(ACCESS_TOKEN_HEADER);

        log.info("Token in request header : {}", token);

        // jwt 유효?
        if (jwtUtils.isAccessTokenValid(token)) {
            log.info("Access token in request header is valid.");

            String userEmail = jwtUtils.getEmailFromAccessToken(token);
            User user = userRepo.findByEmail(userEmail).orElse(null);

            // jwt-userEmail 로 유저 찾음?
            if (user == null) {
                log.warn("No user found with email: {}", userEmail);

                if (!conditionalAuthRequired) {
                    log.error("Failed to authenticate user: {}", userEmail);
                    throw NoUserFoundViaTokenException.Exception;
                }

                logConditionalBypass(NoUserFoundViaTokenException.Exception
                        .getErrorCode().getReason());
            }

            // user 찾았으니까
            // Security Context 에 유저 정보 넣어주기
            else {
                CustomOAuth2User authUser = new CustomOAuth2User(user);
                Authentication authToken = new UsernamePasswordAuthenticationToken(authUser, null,
                        authUser.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("User info were recorded to security context.");
            }
        }

        // jwt 유효하진 않은데 에러는 안 터트려도 됨?
        else if (conditionalAuthRequired) {
            logConditionalBypass(InvalidTokenException.EXCEPTION
                    .getErrorCode().getReason());
        }

        // jwt 필요한데 유효하지 않았음 --> 에러처리
        // 토큰 없던거 아님?
        else if (token == null || token.isEmpty()) {
            log.error("No token exists.");
            throw NoTokenExistsException.EXCEPTION;
        }

        // 토큰 만료된거 아님?
        else if (jwtUtils.isAccessTokenExpired(token)) {
            log.error("Token were expired.");
            throw ExpiredTokenException.EXCEPTION;
        }

        // 모르겠고 invalid
        else {
            log.error("Token is invalid");
            throw InvalidTokenException.EXCEPTION;
        }

        filterChain.doFilter(request, response);
    }


    /**
     * 현 요청을 filter 로직 수행 안해도 되는지 확인
     */
    private boolean isByPassable(HttpServletRequest request) {
        return passable(request, bypassHolder::isByPassable);
    }

    /**
     * filter 를 거치긴 하지만 에러를 터트리지 않아도 되는지 확인
     *
     * @return {@code true} 면 에러 X
     */
    private boolean conditionalAuthRequired(HttpServletRequest request) {
        return passable(request, bypassHolder::isConditionalByPassable);
    }

    /**
     * 우회 여부 판단 내부 메서드
     *
     * @param isPassable {@code true} 면 우회 가능
     */
    private boolean passable(HttpServletRequest request,
            BiFunction<String, HttpMethod, Boolean> isPassable) {
        String requestUri = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        log.info("Request uri: {} - {}", method, requestUri);

        return isPassable.apply(requestUri, method);
    }

    private static void logBypass(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod().toUpperCase();

        log.info("Request matches to bypass list.");
        log.info("Bypassing JwtFilter on request : {} - {}", httpMethod, requestUri);
    }

    private static void logConditionalBypass(String msg) {
        log.info("Bypassing exception due to conditional authentication : {}", msg);
    }
}
