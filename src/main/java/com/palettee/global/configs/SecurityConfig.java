package com.palettee.global.configs;

import com.palettee.global.security.jwt.filters.*;
import com.palettee.global.security.jwt.handler.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.global.security.oauth.*;
import com.palettee.global.security.oauth.handler.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.*;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.web.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginsuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * {@link JwtFilter} 검증을 회피할 {@code URI} 들 {@code (HttpMethod 포함)}
     *
     * @return {@code Map} 의 {@code key} 값은 {@code URI} 와 {@code match} 할 수 있는 {@code regex}
     */
    @Bean
    public Map<String, List<HttpMethod>> byPassableUris() {
        final Map<String, List<HttpMethod>> uris = new HashMap<>();

        // /aaa/{path}      -> /aaa/[^/]+$
        // /aaa/{path}/bbb  -> /aaa/[^/]+/bbb
        // /aaa?query       -> /aaa

        // OAuth2
        uris.put("/login", List.of(HttpMethod.GET));
        uris.put("/login/oauth2/code/google", List.of(HttpMethod.GET));
        uris.put("/login/oauth2/code/github", List.of(HttpMethod.GET));

        // 토큰 발급
        uris.put("/token/issue", List.of(HttpMethod.GET));
        uris.put("/token/reissue", List.of(HttpMethod.POST));
        uris.put("/token/test-test/[^/]+$", List.of(HttpMethod.GET));

        // TODO : 편의용 임시 발급. 나중에 개발 다 되면 없애야 됨.
        uris.put("/token/test-issue", List.of(HttpMethod.POST));

        // 포트폴리오 전체 조회
        uris.put("/portfolio", List.of(HttpMethod.GET));

        // 메인 인기 포트폴리오 페이지
        uris.put("/main/portfolio", List.of(HttpMethod.GET));

        // 소모임 전체 조회
        uris.put("/gathering", List.of(HttpMethod.GET));

        // 아카이브 전체, 단건 조회
        uris.put("/archive", List.of(HttpMethod.GET));
        uris.put("^/archive/[^/]+$", List.of(HttpMethod.GET));

        // 아카이브 검색, 댓글 조회
        uris.put("/archive/search", List.of(HttpMethod.GET));
        uris.put("/archive/[^/]+/comment", List.of(HttpMethod.GET));

        return uris;
    }

    /**
     * {@link JwtFilter} 검증에 특수한 처리가 필요한 {@code URI} 들 {@code (HttpMethod 포함)}
     *
     * @return {@code Map} 의 {@code key} 값은 {@code URI} 와 {@code match} 할 수 있는 {@code regex}
     */
    @Bean
    public Map<String, List<HttpMethod>> conditionalAuthUris() {
        final Map<String, List<HttpMethod>> uris = new HashMap<>();

        // 유저 프로필 정보 조회
        uris.put("/user/[^/]+/profile", List.of(HttpMethod.GET));

        return uris;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        String[] aboveJustNewbie = new String[]{
                UserRole.OLD_NEWBIE.toString(), UserRole.USER.toString(), UserRole.ADMIN.toString()
        };

        // 우리 토큰 쓰니까 아래 것들 꺼도 됨
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // OAuth2 설정
        http
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(endpointConfig ->
                                        endpointConfig.userService(customOAuth2UserService))
                                .successHandler(oauth2LoginsuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler));

        // JwtFilter 추가
        // 토큰 만료로 재로그인 시 JwtFilter 로 무한루프(?) 빠질 수 있음.
        // 그래서 OAuth2 로그인 필터 뒤에 위치
        http
                .addFilterAfter(    // JwtFilter 넣기
                        new JwtFilter(jwtUtils, userRepo, byPassableUris(), conditionalAuthUris()),
                        OAuth2LoginAuthenticationFilter.class)

                // JwtFilter 에서 에러가 터지진 않았지만, 인증되지 않았을 때 행동 지침
                .exceptionHandling(exception ->
                        // 더 높은 권한이 필요
                        exception.accessDeniedHandler(jwtAccessDeniedHandler))
                // JwtFilter 에서 에러 터졌을 때 행동 지침
                .addFilterBefore(new JwtExceptionHandlingFilter(), JwtFilter.class);

        // API 별 authenticate 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error")
                        .permitAll()

                        /* <-------------- User API --------------> */
                        // 토큰 발급
                        .requestMatchers("/token/issue", "/token/reissue")
                        .permitAll()

                        // 유저 정보 입력 폼 관련
                        .requestMatchers("/profile", "/project")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/portfolio")
                        .authenticated()

                        // 유저 정보 수정
                        .requestMatchers("/user/{id}/edit")
                        .authenticated()

                        /* <-------------- Portfolio API --------------> */
                        // 포폴 세부 조회
                        .requestMatchers("/portfolio/{portfolioId}")
                        .hasAnyRole(aboveJustNewbie)

                        // 자신이 좋아요, 북마크한 포폴 조회
                        .requestMatchers("/mypage/like-portfolios")
                        .authenticated()

                        // 포트폴리오 좋아요 누루기
                        .requestMatchers("/portfolio/likes")
                        .authenticated()

                        // 소모임 자기가 생성
                        .requestMatchers(HttpMethod.POST, "/gathering")
                        .hasAnyRole(aboveJustNewbie)

                        // 소모임 상세 정보 조회
                        .requestMatchers("/gathering/{gatheringId}")
                        .hasAnyRole(aboveJustNewbie)

                        /* <-------------- Chat API --------------> */
                        .requestMatchers("/chat-room")
                        .hasAnyRole(aboveJustNewbie)
                        .requestMatchers("/chat-room/**")
                        .hasAnyRole(aboveJustNewbie)
                        .requestMatchers("/chat/**")
                        .hasAnyRole(aboveJustNewbie)
                        .requestMatchers("/chat-img/**")
                        .hasAnyRole(aboveJustNewbie)
                        .requestMatchers("/ws")
                        .hasAnyRole(aboveJustNewbie)

                        /* <-------------- Archive API --------------> */
                        // 아카이브 생성
                        .requestMatchers(HttpMethod.POST, "/archive")
                        .authenticated()

                        // 나의 아카이브 조회 & 내가 좋아요한 아카이브 조회
                        .requestMatchers("/archive/me")
                        .authenticated()
                        .requestMatchers("/archive/me/like")
                        .authenticated()

                        // 아카이브 수정, 삭제, 순서 변경
                        .requestMatchers("/archive/{archiveId}")
                        .authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/archive")
                        .authenticated()

                        // 댓글 작성, 삭제
                        .requestMatchers(HttpMethod.POST, "/archive/{archiveId}/comment")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/archive/{archiveId}/comment")
                        .authenticated()

                        // 알림 조회, 생성, 확인
                        .requestMatchers("/notification/**")
                        .authenticated()

                        /* <-------------- Other API --------------> */
                        .anyRequest()
                        .permitAll()
                );

        // 토큰 이용하니까 stateless session (정확히 뭔지 모르겠음..)
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ));

        return http.build();
    }
}
