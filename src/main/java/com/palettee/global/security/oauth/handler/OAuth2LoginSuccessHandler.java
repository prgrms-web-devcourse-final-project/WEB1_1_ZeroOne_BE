package com.palettee.global.security.oauth.handler;

import com.palettee.global.security.jwt.utils.*;
import com.palettee.global.security.oauth.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.*;

/**
 * OAuth 로그인 성공시 실행되는 handler
 */
@Slf4j
@Component
public class OAuth2LoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final String successRedirectUri;

    public OAuth2LoginSuccessHandler(
            JwtUtils jwtUtils,
            @Value("${oauth-success-redirect-uri}") String successRedirectUri
    ) {
        this.jwtUtils = jwtUtils;
        this.successRedirectUri = successRedirectUri;
    }


    /**
     * 로그인 성공해서 임시 토큰으로 진짜 토큰들 발급하는 {@code /token/issue?token=} API 로 redirect
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User customUserDetail = (CustomOAuth2User) authentication.getPrincipal();
        String temporaryToken = jwtUtils.createTemporaryToken(customUserDetail.getUser());

        log.info("Temporary token issued in CustomLoginSuccessHandler - token: {}", temporaryToken);

        // 임시 토큰으로 access, refresh 토큰 발급하는 endpoint 로 redirect
        // local profile 은 back 으로, deploy 는 프론트 주소로
        response.sendRedirect(successRedirectUri + "?token=" + temporaryToken);
    }
}

