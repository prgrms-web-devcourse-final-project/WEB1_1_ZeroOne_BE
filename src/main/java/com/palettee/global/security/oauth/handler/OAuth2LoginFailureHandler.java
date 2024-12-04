package com.palettee.global.security.oauth.handler;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import com.palettee.global.security.dto.oauth.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
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
        int status = Integer.parseInt(error.getErrorCode());
        String reason = oAuth2AuthenticationException.getMessage();

        OAuth2FailureResponse body = new OAuth2FailureResponse(status, reason,
                LocalDateTime.now().toString());

        // 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.getWriter().write(objectMapper.writeValueAsString(body));

        Object requestUUID = request.getAttribute("custom-request-uuid");

        log.warn("Handled OAuth2 login failure on request [{}] : ",
                requestUUID != null ? requestUUID : "UNKNOWN", exception);
        log.warn("Error status : {}", status);
        log.warn("Error msg : {}", reason);
    }
}
