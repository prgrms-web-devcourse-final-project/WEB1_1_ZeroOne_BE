package com.palettee.global.configs;

import com.palettee.global.security.jwt.filters.*;
import com.palettee.global.security.jwt.handler.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.global.security.oauth.*;
import com.palettee.global.security.oauth.handler.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
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
     * 특정 요청들이 {@link JwtFilter} 를 우회하고 {@link #securityFilterChain} 에 자동 등록도록 하는 {@code Bean}
     * <p>
     * 사용 방법은 {@link BypassUrlHolder} 주석 참고.
     *
     * @author jbw9964
     * @see BypassUrlHolder
     */
    @Bean
    public BypassUrlHolder bypassUrlHolder() {
        return BypassUrlHolder.builder()
                .byPassable("/error")
                .byPassable("/actuator/**")
                .byPassable("/health-check")

                // webSocket
                .byPassable("/index.html")
                .byPassable("/ws")

                // swagger
                .byPassable("/api-test")
                .byPassable("/swagger-ui/**")
                .byPassable("/favicon.ico")
                .byPassable("/v3/api-docs/**")

                /* <-------------- User API --------------> */
                // OAuth2
                .byPassable("/login", "/login/oauth2/code/*")

                // 토큰 발급
                .byPassable("/token/issue", HttpMethod.GET)
                .byPassable("/token/reissue", HttpMethod.POST)

                // TODO : 편의용 임시 발급. 나중에 개발 다 되면 없애야 됨.
                .byPassable("/token/test-issue", HttpMethod.POST)

                // 유저의 프로필, 아카이브, 게더링 조회
                .conditionalByPassable("/user/{id}/profile", HttpMethod.GET)
                .byPassable(HttpMethod.GET,
                        "/user/{id}/archive-colors", "/user/{id}/archives", "/user/{id}/gatherings")

                // 유저 제보 목록, 상세 내용, 댓글 조회
                .byPassable(HttpMethod.GET, "/report", "/report/{reportId}",
                        "/report/{reportId}/comment")

                /* <-------------- Portfolio API --------------> */
                // 포트폴리오 전체 조회
                .byPassable("/portFolio", HttpMethod.GET)

                // 메인 인기 포트폴리오 페이지
                .byPassable("/portFolio/main", HttpMethod.GET)

                // 소모임 전체 조회
                .byPassable("/gathering", HttpMethod.GET)

                /* <-------------- Archive API --------------> */
                // 아카이브 전체 & 단건 조회 & 아카이브 검색 & 댓글 조회
                .conditionalByPassable(HttpMethod.GET, "/archive/{archiveId}")
                .conditionalByPassable(HttpMethod.GET, "/archive/main")
                .conditionalByPassable(HttpMethod.GET, "/archive")
                .conditionalByPassable(HttpMethod.GET, "/archive/search")
                .conditionalByPassable(HttpMethod.GET, "/archive/search")
                .conditionalByPassable(HttpMethod.GET, "/archive/{archiveId}/comment")

                .build();
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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            BypassUrlHolder bypassHolder
    ) throws Exception {

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

        //Custome한 Cors 설정 넣기 위해서

        http
                .cors();

        // JwtFilter 추가
        // 토큰 만료로 재로그인 시 JwtFilter 로 무한루프(?) 빠질 수 있음.
        // 그래서 OAuth2 로그인 필터 뒤에 위치
        http
                .addFilterAfter(    // JwtFilter 넣기
                        new JwtFilter(jwtUtils, userRepo, bypassHolder),
                        OAuth2LoginAuthenticationFilter.class)

                // JwtFilter 에서 에러가 터지진 않았지만, 인증되지 않았을 때 행동 지침
                .exceptionHandling(exception ->
                        // 더 높은 권한이 필요
                        exception.accessDeniedHandler(jwtAccessDeniedHandler))
                // JwtFilter 에서 에러 터졌을 때 행동 지침
                .addFilterBefore(new JwtExceptionHandlingFilter(), JwtFilter.class);

        String oldNewbie = UserRole.OLD_NEWBIE.toString().toUpperCase();
        String admin = UserRole.ADMIN.toString().toUpperCase();

        // API 별 authenticate 설정
        http
                .authorizeHttpRequests(auth -> {
                    /* <<<--------- bypass 인 요청들 등록 --------->>> */
                    // bypass 로 등록된 요청목록들 permitAll
                    bypassHolder.registerByPasses(auth);

                    /* <<<--------- 나머지 인증 필요한 요청들 등록 --------->>> */
                    auth
                            /* <-------------- User API --------------> */
                            // 유저 제보를 해결함으로 변경 (관리자만 가능)
                            .requestMatchers(HttpMethod.PATCH, "/report/{reportId}/fixed")
                            .hasRole(admin)

                            /* <-------------- Portfolio API --------------> */
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

                            .requestMatchers(HttpMethod.POST, "/gathering")
                            .hasRole(oldNewbie)

                            /* <-------------- Other API --------------> */
                            .anyRequest()
                            .authenticated();
                });

        // 토큰 이용하니까 stateless session (정확히 뭔지 모르겠음..)
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ));

        return http.build();
    }
}
