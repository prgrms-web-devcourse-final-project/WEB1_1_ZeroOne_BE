package com.palettee.global.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.slf4j.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.filter.*;

/**
 * {@code Mapped Diagnostic Context} 이용한 도메인별 로그 저장(은 아니긴 한데 비슷한) {@code filter}
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 이러면 filter 중에서도 가장 빠른? 필터로 되는듯. 신기하네
public class MDCLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 요청별 랜덤 uuid 생성 & 저장
        String requestUUID = UUID.randomUUID().toString();
        request.setAttribute("custom-request-uuid", requestUUID);
        log.info("custom-request-uuid {} has been set to request {} \"{}\"",
                requestUUID, request.getMethod(), request.getRequestURI());

        try {

            this.configureLogDirViaUri(request);
            log.info("============== REQUEST [{}] START ==============", requestUUID);
            filterChain.doFilter(request, response);

        } finally {
            log.info("============== REQUEST [{}] END ==============", requestUUID);
            MDC.clear();
        }
    }

    // uri 별 도메인 파악해서 MDC key 넣어주기
    private void configureLogDirViaUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        DomainType domainType = DomainType.of(uri);

        MDC.put("DOMAIN_LOG_DIR", domainType.getDomainLogDir());

        log.info("Domain logging directory has been set to : [{}]", domainType.getDomainLogDir());
    }
}
