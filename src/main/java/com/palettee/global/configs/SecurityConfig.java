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
import org.springframework.security.access.hierarchicalroles.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.*;
import org.springframework.security.oauth2.client.web.*;
import org.springframework.security.web.*;

@Configuration
@EnableWebSecurity
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

        // TODO : 편의용 임시 발급. 나중에 개발 다 되면 없애야 됨.
        uris.put("/token/test-issue", List.of(HttpMethod.POST));

        // 포트폴리오 전체 조회
        uris.put("/portFolio", List.of(HttpMethod.GET));

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
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_USER
                ROLE_USER > ROLE_OLD_NEWBIE
                ROLE_OLD_NEWBIE > ROLE_JUST_NEWBIE
                ROLE_JUST_NEWBIE > ROLE_REAL_NEWBIE
                ROLE_REAL_NEWBIE > ROLE_AUTHENTICATED
                ROLE_AUTHENTICATED > ROLE_UNAUTHENTICATED
                """);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

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

        String oldNewbie = UserRole.OLD_NEWBIE.toString().toUpperCase();

        // API 별 authenticate 설정
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error")
                        .permitAll()

                        /* <-------------- User API --------------> */
                        // OAuth2
                        .requestMatchers("/login")
                        .permitAll()
                        .requestMatchers("/login/oauth2/code/*")
                        .permitAll()

                        // 토큰 발급
                        .requestMatchers("/token/issue", "/token/reissue")
                        .permitAll()

                        // TODO : 편의용 임시 발급
                        .requestMatchers("/token/test-issue")
                        .permitAll()

                        /* <-------------- Portfolio API --------------> */
                        // 포폴 전체 조회
                        .requestMatchers(HttpMethod.GET, "/portFolio")
                        .permitAll()


                        // 메인 인기 포트폴리오 페이지
                        .requestMatchers("/main/portfolio")
                        .permitAll()

                        // 소모임 전체 조회
                        .requestMatchers(HttpMethod.GET, "/gathering")
                        .permitAll()

                        // 포폴 등록한 사람부터 가능
                        // 포폴 세부 조회
                        .requestMatchers("/portfolio/{portfolioId}")
                        .hasRole(oldNewbie)

                        // 소모임 자기가 생성 - 포폴 등록한 사람부터 가능
                        .requestMatchers(HttpMethod.POST, "/gathering")
                        .hasRole(oldNewbie)

                        // 소모임 상세 정보 조회
                        .requestMatchers("/gathering/{gatheringId}")
                        .hasRole(oldNewbie)

                        /* <-------------- Chat API --------------> */
                        .requestMatchers("/chat-room")      // 채팅방 생성, 내 채팅방 목록 조회
                        .hasRole(oldNewbie)
                        .requestMatchers("/chat-room/**")   // 채팅방 참여, 나가기
                        .hasRole(oldNewbie)
                        .requestMatchers("/chat/**")        // 채팅 조회
                        .hasRole(oldNewbie)
                        .requestMatchers("/chat-img/**")    // 사진 업로드
                        .hasRole(oldNewbie)
                        .requestMatchers("/ws")             // 채팅 보내기, 채팅 목록 실시간 (웹소켓)
                        .hasRole(oldNewbie)

                        /* <-------------- Archive API --------------> */
                        // 아카이브 전체 조회
                        .requestMatchers(HttpMethod.GET, "/archive")
                        .permitAll()

                        // 아카이브 단건 조회
                        .requestMatchers(HttpMethod.GET, "/archive/{archiveId}")
                        .permitAll()

                        // 아카이브 검색
                        .requestMatchers(HttpMethod.GET, "/archive/search")
                        .permitAll()

                        // 아카이브 댓글 조회
                        .requestMatchers(HttpMethod.GET, "/archive/{archiveId}/comment")
                        .permitAll()


                        /* <-------------- Other API --------------> */
                        .anyRequest()
                        .authenticated()
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
