package com.palettee.global.configs;

import com.palettee.global.security.oauth.*;
import com.palettee.global.security.oauth.handler.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.*;
import org.springframework.security.web.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oauth2LoginsuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        // 우리 토큰 쓰니까 아래 것들 꺼도 됨
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // OAuth2 설정
        // CustomOAuth2 만들어지면 수정 해야됨.
        http
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(endpointConfig ->
                                        endpointConfig.userService(customOAuth2UserService))
                                .successHandler(oauth2LoginsuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler));

        // API 별 authenticate 설정
        // 일단 permitAll
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/").permitAll()

                        .anyRequest().permitAll()
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
