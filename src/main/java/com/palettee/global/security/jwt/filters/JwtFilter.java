package com.palettee.global.security.jwt.filters;

import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.global.security.oauth.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
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

    private final Map<String, List<HttpMethod>> byPassableUris;
    private final Map<String, List<HttpMethod>> conditionalAuthUris;

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

                logConditionalBypass(NoUserFoundViaTokenException.Exception.getMessage());
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
            logConditionalBypass(InvalidTokenException.EXCEPTION.getMessage());
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
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod().toUpperCase();

        log.info("Request uri: {} - {}", httpMethod, requestUri);

        return hasMatch(byPassableUris, requestUri, httpMethod);
    }

    /**
     * filter 를 거치긴 하지만 에러를 터트리지 않아도 되는지 확인
     *
     * @return {@code true} 면 에러 X
     */
    private boolean conditionalAuthRequired(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod().toUpperCase();

        return hasMatch(conditionalAuthUris, requestUri, httpMethod);
    }


    /**
     * 현재 요청이 정의된 {@code uriRegexMap} 에 포함되는지 알려주는 메서드
     *
     * @param uriRegexMap 확인할 {@code URI} 의 정규 표현식 + {@link  HttpMethod}
     * @param uri         검사할 요청의 {@code URI}
     * @param httpMethod  검사할 요청의 {@code HttpMethod} (대문자)
     * @return 현재 요청이 {@code uriRegexMap} 에 속해 있다면 {@code true}
     */
    private boolean hasMatch(Map<String, List<HttpMethod>> uriRegexMap, String uri,
            String httpMethod) {
        // key 값 중 matches 인 url 뱉기
        String matchingUrl = uriRegexMap.keySet().parallelStream()
                .filter(uri::matches)
                .findFirst().orElse(null);

        // match 하는 url 가 존재하고 httpMethod 도 동일하면 true
        return matchingUrl != null &&
                uriRegexMap.get(matchingUrl).stream()
                        .map(HttpMethod::name)
                        .map(String::toUpperCase)
                        .anyMatch(httpMethod::equals);
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
