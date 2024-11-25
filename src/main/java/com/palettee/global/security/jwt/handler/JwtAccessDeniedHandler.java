package com.palettee.global.security.jwt.handler;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import com.palettee.global.exception.*;
import com.palettee.global.security.dto.token.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.web.access.*;
import org.springframework.stereotype.*;

/**
 * Jwt 로 유저 확인은 가능하나 더 높은 권한이 필요할 때 행동하는 handler
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Higher authorize required in request: {} - {}", request.getMethod(),
                request.getRequestURI());

        ErrorCode err = ErrorCode.ROLE_MISMATCH;
        int status = err.getStatus();
        String reason = err.getReason();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        FilterExceptionResponse body = new FilterExceptionResponse(
                status, reason, LocalDateTime.now().toString()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.getWriter().write(objectMapper.writeValueAsString(body));

        log.error("JwtAccessDeniedHandler handled exception: {}",
                accessDeniedException.getClass().getSimpleName());
        log.error("Exception message: {}", accessDeniedException.getMessage());
    }
}
