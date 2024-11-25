package com.palettee.global.security.jwt.utils;

import com.palettee.global.security.jwt.filters.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.util.*;

/**
 * JwtFilter 를 우회할 {@code URI} 를 등록하고 {@code SecurityFilterChain} 에 {@code .permitAll} 해주는 편의용 클래스
 * <p>
 * {@code URI} 패턴 사용법 - {@link AntPathMatcher} : {@code SecurityFilterChain} 에서 등록하는 방법과 동일 (우리 맨날
 * 하던거)
 *
 * <ul>
 * <li>{@code ?} 한 글자 존재</li>
 * <li>{@code *} 0 개 이상의 글자 존재</li>
 * <li>{@code **} 0 개 이상의 <em>경로</em> 존재</li>
 * <li>{@code {???}} {@code Path Variable} 표현</li>
 * </ul>
 *
 * <br><hr><br>
 *
 * <h3>사용법 :</h3>
 * <pre>
 *     {@code
 *     BypassUrlHolder byPass = BypassUrlHolder.builder()
 *          // ---- 무조건 필터 우회할 목록들
 *
 *          // `/aaa/{id}/bbb` 의 get, option 등록
 *          .byPassable("/aaa/{id}/bbb", HttpMethod.GET, HttpMethod.OPTION)
 *          // `/aa/{id}` 인 모든 httpMethod 등록
 *          .byPassable("/aa/{id}")
 *          // `/bb`, `/cc/~~` 의 get 등록
 *          .byPassable(HttpMethod.GET, "/bb", "/cc/**")
 *
 *          // ---- 조건부 우회할 목록들 (jwt 존재하면 Security context 저장, 없으면 filterChain)
 *
 *          // `/cond/~~` 인 모든 httpMethod 등록
 *          .conditionalByPassable("/cond/**")
 *          .build();
 *     }
 * </pre>
 * <p>
 * {@code BypassUrlHolder} 를 생성 후, 아래처럼 {@code SecurityFilterChain} 에 정보를 등록할 수 있습니다.
 * <p>
 * <b>{@code .byPassable}, {@code .conditionalByPassable} 로 등록된 요청들은 모두 {@code permitAll} 로 설정됩니다.</b>
 *
 * <pre>
 *     {@code
 *     @Bean
 *     public SecurityFilterChain securityFilterChain(
 *          HttpSecurity http, BypassUrlHolder byPassHolder) throws Exception {
 *
 *          http
 *              .authorizeHttpRequests(auth -> {
 *                      // bypass 로 등록된 요청목록들 permitAll
 *                      auth = byPassHolder.recordByPassablePaths(auth);
 *
 *                      // 나머지 등록 (잊지 말기)
 *                      auth
 *                          .requestMatchers("/portfolio/{portfolioId}")
 *                          .hasRole(oldNewbie)
 *
 *                          .requestMatchers(HttpMethod.POST, "/gathering")
 *                          .hasRole(oldNewbie)
 *
 *                          .anyRequest()
 *                          .authenticated();
 *                 });
 *
 *          return http.build();
 *     }
 * </pre>
 *
 * @author jbw9964
 */
@Slf4j
public class BypassUrlHolder {

    // URI 패턴 matcher
    private final AntPathMatcher pathMatcher;

    // 우회할 요청 정보들
    @Getter
    private final List<RequestInfoHolder> byPassableRequests;
    @Getter
    private final List<RequestInfoHolder> conditionalByPassableRequests;


    /**
     * 우회할 {@code URI}, {@code HttpMethod} 를 저장하기 위한 내부 클래스
     *
     * @param uriPatterns 우회할 {@code URI} 패턴
     * @param httpMethods 우회할 {@code URI} 들의 {@code HTTP Method}
     */
    public record RequestInfoHolder(
            List<String> uriPatterns, Set<HttpMethod> httpMethods
    ) {

        /**
         * 주어진 요청 {@code (uri, httpMethod)} 가 저장된 정보와 match 되는지 확인
         *
         * @param matcher    Ant 패턴 일치 확인할 {@link AntPathMatcher}
         * @param uri        요청 {@code URI}
         * @param httpMethod 요청 {@code HttpMethod}
         */
        boolean matches(AntPathMatcher matcher,
                String uri, HttpMethod httpMethod) {

            // uri 일치 확인
            boolean uriMatches = uriPatterns.stream()
                    .anyMatch(pattern ->
                            matcher.match(pattern, uri));

            // uri 일치하고 method 도 일치하면 true
            return uriMatches && httpMethods.contains(httpMethod);
        }
    }

    // 생성자 사용하지 말고 builder 사용
    private BypassUrlHolder(
            List<RequestInfoHolder> byPassableRequests,
            List<RequestInfoHolder> conditionalByPassableRequests
    ) {
        pathMatcher = new AntPathMatcher();
        this.byPassableRequests = byPassableRequests;
        this.conditionalByPassableRequests = conditionalByPassableRequests;
    }

    /**
     * {@link BypassUrlHolder} builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@link BypassUrlHolder} builder
     */
    public static class Builder {

        List<RequestInfoHolder> byPassableRequests;
        List<RequestInfoHolder> conditionalByPassableRequests;

        private Builder() {
            byPassableRequests = new ArrayList<>();
            conditionalByPassableRequests = new ArrayList<>();
        }

        /**
         * 우회 정보 저장하는 내부 메서드
         *
         * @param dst         {@code (무조건 우회 목록)} or {@code (조건부 우회 목록)}
         * @param uriPatterns 저장할 {@code URI} 패턴들
         * @param httpMethods {@code URI} 패턴들의 {@code HttpMethod}
         */
        private Builder addRequest(List<RequestInfoHolder> dst,
                List<String> uriPatterns,
                Set<HttpMethod> httpMethods) {

            log.info("Add {}-{} request to {}", httpMethods, uriPatterns,
                    dst.equals(byPassableRequests) ? "Bypass" : "Conditional");

            dst.add(new RequestInfoHolder(uriPatterns, httpMethods));
            return this;
        }

        /**
         * 무조건 {@link JwtFilter} 우회
         * <p>
         * 요청이 주어진 {@code uriPattern} 과 일치하고 {@code httpMethods} 중 하나이면 우회
         *
         * @param uriPattern  무조건 우회할 {@code URI} 패턴
         * @param httpMethods 허용할 {@code HttpMethod}
         */
        public Builder byPassable(String uriPattern, HttpMethod... httpMethods) {
            return this.addRequest(byPassableRequests,
                    List.of(uriPattern), Set.of(httpMethods));
        }

        /**
         * 무조건 {@link JwtFilter} 우회
         * <p>
         * 요청 method 가 {@code httpMethod} 이고 {@code uriPatterns} 중 일치하는 것이 있으면 우회
         *
         * @param httpMethod  허용할 {@code HttpMethod}
         * @param uriPatterns 우회할 {@code URI} 패턴들
         */
        public Builder byPassable(HttpMethod httpMethod, String... uriPatterns) {
            return this.addRequest(byPassableRequests,
                    List.of(uriPatterns), Set.of(httpMethod));
        }

        /**
         * 무조건 {@link JwtFilter} 우회
         * <p>
         * 요청이 {@code uriPattern} 과 일치하면 우회 {@code (모든 HttpMethod 허용)}
         */
        public Builder byPassable(String uriPattern) {
            return this.addRequest(byPassableRequests,
                    List.of(uriPattern), getAllMethods());
        }

        /**
         * 무조건 {@link JwtFilter} 우회
         * <p>
         * 요청이 {@code uriPatterns} 중 아무거나 일치하면 우회 {@code (모든 HttpMethod 허용)}
         */
        public Builder byPassable(String... uriPatterns) {
            return this.addRequest(byPassableRequests,
                    List.of(uriPatterns), getAllMethods());
        }

        /**
         * 조건부 {@link JwtFilter} 우회
         * <p>
         * {@code SecurityContext} 에 정보 넣을 수 있으면 넣고, 못 넣으면 다음 filter 수행
         * <p>
         * 요청이 {@code uriPattern} 과 일치하고 {@code httpMethods} 중 하나이면 조건부 우회
         *
         * @param uriPattern  조건부 우회할 {@code URI} 패턴
         * @param httpMethods 조건부 우회할 {@code HttpMethod}
         */
        public Builder conditionalByPassable(String uriPattern, HttpMethod... httpMethods) {
            return this.addRequest(conditionalByPassableRequests,
                    List.of(uriPattern), Set.of(httpMethods));
        }

        /**
         * 조건부 {@link JwtFilter} 우회
         * <p>
         * {@code SecurityContext} 에 정보 넣을 수 있으면 넣고, 못 넣으면 다음 filter 수행
         * <p>
         * 요청 method 가 {@code httpMethod} 이고 {@code uriPatterns} 중 일치하는 것이 있으면 조건부 우회
         *
         * @param httpMethod  허용할 {@code HttpMethod}
         * @param uriPatterns 우회할 {@code URI} 패턴들
         */
        public Builder conditionalByPassable(HttpMethod httpMethod, String... uriPatterns) {
            return this.addRequest(conditionalByPassableRequests,
                    List.of(uriPatterns), Set.of(httpMethod));
        }

        /**
         * 조건부 {@link JwtFilter} 우회
         * <p>
         * {@code SecurityContext} 에 정보 넣을 수 있으면 넣고, 못 넣으면 다음 filter 수행
         * <p>
         * 요청이 {@code uriPattern} 과 일치하면 조건부 우회 {@code (모든 HttpMethod 허용)}
         */
        public Builder conditionalByPassable(String uriPattern) {
            return this.addRequest(conditionalByPassableRequests,
                    List.of(uriPattern), getAllMethods());
        }

        /**
         * 조건부 {@link JwtFilter} 우회
         * <p>
         * {@code SecurityContext} 에 정보 넣을 수 있으면 넣고, 못 넣으면 다음 filter 수행
         * <p>
         * 요청이 {@code uriPatterns} 중 아무거나 일치하면 조건부 우회 {@code (모든 HttpMethod 허용)}
         */
        public Builder conditionalByPassable(String... uriPatterns) {
            return this.addRequest(conditionalByPassableRequests,
                    List.of(uriPatterns), getAllMethods());
        }

        /**
         * {@link BypassUrlHolder} builder
         */
        public BypassUrlHolder build() {
            return new BypassUrlHolder(byPassableRequests,
                    conditionalByPassableRequests);
        }

        // 모든 HttpMethod 가져오는 편의용 내부 메서드
        private Set<HttpMethod> getAllMethods() {
            return Set.of(
                    HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
                    HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.TRACE
            );
        }
    }


    /**
     * 주어진 요청이 {@link JwtFilter} 무조건 우회 요청인지 확인
     *
     * @param uri        요청 {@code URI}
     * @param httpMethod 요청 {@code HttpMethod}
     * @return 무조건 우회 요청이면 {@code true}
     */
    public boolean isByPassable(String uri, HttpMethod httpMethod) {
        return matches(this.byPassableRequests,
                uri, httpMethod);
    }

    /**
     * 주어진 요청이 {@link JwtFilter} 조건부 우회 요청인지 확인
     *
     * @param uri        요청 {@code URI}
     * @param httpMethod 요청 {@code HttpMethod}
     * @return 조건부 우회 요청이면 {@code true}
     */
    public boolean isConditionalByPassable(String uri, HttpMethod httpMethod) {
        return matches(this.conditionalByPassableRequests,
                uri, httpMethod);
    }

    /**
     * 요청 match 확인하는 내부 메서드
     */
    private boolean matches(
            List<RequestInfoHolder> passableRequests,
            String uri, HttpMethod httpMethod) {

        return passableRequests.parallelStream()
                .anyMatch(p -> p.matches(this.pathMatcher, uri, httpMethod));
    }

    /**
     * 저장된 {@code (무조건 우회 요청)} 과 {@code (조건부 우회 요청)} 을 {@code auth} 에 등록하는 메서드
     * <p>
     * <b>이 때 모든 요청은 {@code .permitAll()} 됨에 주의</b>
     *
     * @param auth {@code  http.authorizeHttpRequests(auth -> ... )} 에서 쓰는 {@code auth}
     */
    public void registerByPasses(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {

        log.info("Registering bypass URIs to authorizeHttpRequests...");

        // 무조건 우회 목록들 등록
        log.debug("Registering absolute bypass URIs");
        this.register(auth, byPassableRequests);

        // 조건부 우회 목록들 등록
        log.debug("Registering conditional bypass URIs");
        this.register(auth, conditionalByPassableRequests);

        log.info("All bypass URIs were registered.");
    }

    private void register(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            List<RequestInfoHolder> bypasses) {

        for (RequestInfoHolder request : bypasses) {

            // 우회할 URI 패턴
            List<String> uriPatterns = request.uriPatterns;
            logGivenUris(uriPatterns);
            logHttpMethods(request.httpMethods);

            // HttpMethod 는 많아야 8 개
            for (HttpMethod method : request.httpMethods) {
                auth = auth.requestMatchers(method, uriPatterns.toArray(new String[]{}))
                        .permitAll();
            }

            logRegistered();
        }
    }

    private static void logGivenUris(List<String> uriPatterns) {
        log.debug("Given URIs: {}", uriPatterns);
    }

    private static void logHttpMethods(Set<HttpMethod> httpMethods) {
        log.debug("Permitted http methods: {}", httpMethods);
    }

    private static void logRegistered() {
        log.debug("Given URIs were registered.");
    }
}
