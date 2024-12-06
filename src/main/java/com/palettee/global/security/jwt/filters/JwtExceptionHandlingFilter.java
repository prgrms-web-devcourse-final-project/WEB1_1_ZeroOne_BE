package com.palettee.global.security.jwt.filters;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.*;
import com.palettee.global.exception.*;
import com.palettee.global.security.dto.token.*;
import com.palettee.global.security.jwt.exceptions.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.filter.*;

/**
 * JwtFilter 에서 발생한 exception 들을 잡아 응답하기 위한 filter
 */
@Slf4j
public class JwtExceptionHandlingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredTokenException | InvalidTokenException | NoTokenExistsException
                 | NoUserFoundViaTokenException | RoleMismatchException e) {

            log.warn("Jwt exception occurred at {}", request.getRequestURI());

            ErrorCode err = e.getErrorCode();
            responseErr(err.getStatus(), err.getReason(), e, response, request);
        } catch (Exception e) {
            log.error("Unexpected exception occurred at {}", request.getRequestURI());

            responseErr(500, e.getMessage(), e, response, request);
        }
    }

    private void responseErr(int status, String reason, Exception e, HttpServletResponse resp,
            HttpServletRequest req)
            throws IOException {
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);

        FilterExceptionResponse response = new FilterExceptionResponse(
                status, reason, LocalDateTime.now().toString()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        resp.getWriter().write(objectMapper.writeValueAsString(response));

        Object requestUUID = req.getAttribute("custom-request-uuid");

        log.error(
                "On REQUEST [{} \"{}\"] - [{}], JwtExceptionHandlingFilter handled exception : {}",
                req.getMethod(), req.getRequestURL(),
                requestUUID, e.getClass().getSimpleName(), e);
    }
}
