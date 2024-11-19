package com.palettee.global.security.oauth.handler;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public class OAuth2LoginFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    /**
     * {@link com.palettee.global.security.oauth.CustomOAuth2UserService} 에서 실패시 동작하는 handler
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        OAuth2AuthenticationException oAuth2AuthenticationException
                = (OAuth2AuthenticationException) exception;
        OAuth2Error error = oAuth2AuthenticationException.getError();

        // throws 된 exception 에서 코드, 원인 뽑아내기
        int errorCode = Integer.parseInt(error.getErrorCode());
        String reason = oAuth2AuthenticationException.getMessage();

        // 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode);

        Map<String, Object> body = new HashMap<>();
        body.put("reason", reason);
        body.put("timestamp", Instant.now());

        response.getWriter().write(body.toString());

        log.warn("Handled OAuth2 login failure.");
        log.warn("Error status : {}", errorCode);
        log.warn("Error msg : {}", reason);
    }
}
