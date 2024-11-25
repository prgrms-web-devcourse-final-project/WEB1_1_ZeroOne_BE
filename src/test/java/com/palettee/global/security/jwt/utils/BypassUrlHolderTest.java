package com.palettee.global.security.jwt.utils;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.palettee.global.exception.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.handler.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import java.util.*;
import java.util.function.*;
import lombok.extern.slf4j.*;
import org.hamcrest.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class BypassUrlHolderTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    BypassUrlHolder bypassHolder;

    @SpyBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtUtils jwtUtils;

    // httpMethod, url 로 MockHttpServletRequestBuilder 만드는 편의용 개체
    private final BiFunction<HttpMethod, String, MockHttpServletRequestBuilder> requestBuilder = (method, url)
            -> switch (method.name()) {
        case "GET" -> get(url);
        case "POST" -> post(url);
        case "PUT" -> put(url);
        case "DELETE" -> delete(url);
        case "HEAD" -> head(url);
        case "OPTIONS" -> options(url);
        case "PATCH" -> patch(url);
        // trace 는 MockMvc 에 없네? 그냥 get 으로 대체
        default -> get(url);
    };


    @Test
    @DisplayName("무조건 통과 로직 확인")
    void testAbsoluteBypass() {

        String permitAllMethods = "/random1";
        String permitFewMethods = "/random2";
        String pathVariable = "/random3/{id}";
        String innerPathVariable = "/random4/{id}/random";

        BypassUrlHolder testHolder = BypassUrlHolder.builder()
                .byPassable(permitAllMethods)
                .byPassable(permitFewMethods, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                .byPassable(pathVariable, HttpMethod.GET, HttpMethod.TRACE, HttpMethod.OPTIONS)
                .byPassable(innerPathVariable, HttpMethod.DELETE, HttpMethod.HEAD)
                .build();

        // 모든 method 통과 되는지 확인
        this.checkPassable(testHolder::isByPassable, permitAllMethods,
                getAllMethods().toArray(new HttpMethod[]{}));

        // 일부 method 만 통과 잘 되는지 확인
        this.checkPassable(testHolder::isByPassable, permitFewMethods,
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

        Random rand = new Random();

        // path variable 있어도 잘 되는지 확인 1
        String randUri = pathVariable.replaceAll("\\{id}", String.valueOf(rand.nextInt()));
        this.checkPassable(testHolder::isByPassable, randUri,
                HttpMethod.GET, HttpMethod.TRACE, HttpMethod.OPTIONS);

        // path variable 있어도 잘 되는지 확인 2
        randUri = innerPathVariable.replaceAll("\\{id}", String.valueOf(rand.nextInt()));
        this.checkPassable(testHolder::isByPassable, randUri,
                HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    @DisplayName("조건부 통과 로직 확인")
    void testConditionalBypass() {

        String permitAllMethods = "/random1";
        String permitFewMethods = "/random2";
        String pathVariable = "/random3/{id}";
        String innerPathVariable = "/random4/{id}/random";

        BypassUrlHolder testHolder = BypassUrlHolder.builder()
                .conditionalByPassable(permitAllMethods)
                .conditionalByPassable(permitFewMethods, HttpMethod.POST, HttpMethod.PUT,
                        HttpMethod.DELETE)
                .conditionalByPassable(pathVariable, HttpMethod.GET, HttpMethod.TRACE,
                        HttpMethod.OPTIONS)
                .conditionalByPassable(innerPathVariable, HttpMethod.DELETE, HttpMethod.HEAD)
                .build();

        // 모든 method 통과 되는지 확인
        this.checkPassable(testHolder::isConditionalByPassable, permitAllMethods,
                getAllMethods().toArray(new HttpMethod[]{}));

        // 일부 method 만 통과 잘 되는지 확인
        this.checkPassable(testHolder::isConditionalByPassable, permitFewMethods,
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

        Random rand = new Random();

        // path variable 있어도 잘 되는지 확인 1
        String randUri = pathVariable.replaceAll("\\{id}", String.valueOf(rand.nextInt()));
        this.checkPassable(testHolder::isConditionalByPassable, randUri,
                HttpMethod.GET, HttpMethod.TRACE, HttpMethod.OPTIONS);

        // path variable 있어도 잘 되는지 확인 2
        randUri = innerPathVariable.replaceAll("\\{id}", String.valueOf(rand.nextInt()));
        this.checkPassable(testHolder::isConditionalByPassable, randUri,
                HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    @DisplayName("MVC 로 무조건 통과 요청 PermitAll 한지 확인")
    void testAbsoluteByPassWithRequest() throws Exception {

        // 무조건 통과 요청 확인
        for (var bypass : bypassHolder.getByPassableRequests()) {

            // uri 패턴에서 url 만들어주기
            List<String> randomUrls = genRandomUrls(bypass.uriPatterns());

            // permitAll 했으니까
            // 권한이 부족해 일어나는 에러는 없어야 함
            for (HttpMethod httpMethod : bypass.httpMethods()) {
                for (String url : randomUrls) {

                    // MockMvc 실행
                    mvc.perform(requestBuilder.apply(httpMethod, url));

                    // 권한 부족 handler 실행된 적 X
                    Mockito.verify(jwtAccessDeniedHandler, Mockito.never())
                            .handle(Mockito.any(), Mockito.any(), Mockito.any());
                }
            }
        }
    }

    @Test
    @DisplayName("MVC 로 조건부 통과 요청 PermitAll 한지 확인")
    void testConditionalByPassWithRequest() throws Exception {

        // 토큰 없을 때 조건부 통과 요청 확인
        for (var conditional : bypassHolder.getConditionalByPassableRequests()) {

            // uri 패턴에서 url 만들어주기
            List<String> randomUrls = genRandomUrls(conditional.uriPatterns());

            // 토큰 없어도 permitAll 했으니까
            // 권한이 부족해 일어나는 에러는 없어야 함
            for (HttpMethod httpMethod : conditional.httpMethods()) {
                for (String url : randomUrls) {

                    // 실행
                    mvc.perform(requestBuilder.apply(httpMethod, url));

                    // 권한 부족 handler 실행된 적 X
                    Mockito.verify(jwtAccessDeniedHandler, Mockito.never())
                            .handle(Mockito.any(), Mockito.any(), Mockito.any());
                }
            }

        }

        // 토큰 있으면 SecurityContext 에 유저 정보 있어야 함
        User testUser = userRepo.save(
                User.builder()
                        .email("test@test.com")
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );

        String accessToken = jwtUtils.createAccessToken(testUser);

        for (var conditional : bypassHolder.getConditionalByPassableRequests()) {

            // uri 패턴에서 url 만들어주기
            List<String> randomUrls = genRandomUrls(conditional.uriPatterns());

            // 토큰 없어도 permitAll 했으니까
            // 권한이 부족해 일어나는 에러는 없어야 함
            for (HttpMethod httpMethod : conditional.httpMethods()) {
                for (String url : randomUrls) {

                    // 실행 - 토큰 없을 때
                    mvc.perform(requestBuilder.apply(httpMethod, url));

                    // 권한 부족 handler 실행된 적 X
                    Mockito.verify(jwtAccessDeniedHandler, Mockito.never())
                            .handle(Mockito.any(), Mockito.any(), Mockito.any());

                    // 실행 - 토큰 있을 때
                    ResultActions resultActions = mvc.perform(requestBuilder.apply(httpMethod, url)
                            .header("Authorization", accessToken));

                    // 권한 부족 handler 실행된 적 X
                    Mockito.verify(jwtAccessDeniedHandler, Mockito.never())
                            .handle(Mockito.any(), Mockito.any(), Mockito.any());

                    // 토큰 관련 오류 없음
                    for (var tokenExceptions : tokenRelatedExceptions()) {
                        String reason = tokenExceptions.getErrorCode().getReason();

                        resultActions.andExpect(content().string(
                                doesNotContainString(reason)
                        ));
                    }
                }
            }
        }
    }

    private Matcher<String> doesNotContainString(String s) {
        return CoreMatchers.not(containsString(s));
    }

    private void checkPassable(BiFunction<String, HttpMethod, Boolean> isPassable, String uri,
            HttpMethod... validMethods) {
        Set<HttpMethod> invalidMethods = getMethodsExcept(validMethods);

        for (HttpMethod method : validMethods) {
            assertThat(isPassable.apply(uri, method))
                    .isTrue();
        }

        for (HttpMethod method : invalidMethods) {
            assertThat(isPassable.apply(uri, method))
                    .isFalse();
        }
    }

    private Set<HttpMethod> getAllMethods() {
        return Set.of(
                HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
                HttpMethod.OPTIONS, HttpMethod.PATCH, HttpMethod.TRACE, HttpMethod.HEAD
        );
    }

    private Set<HttpMethod> getMethodsExcept(HttpMethod... methods) {
        Set<HttpMethod> result = new HashSet<>(getAllMethods());
        Arrays.asList(methods).forEach(result::remove);
        return result;
    }

    private List<String> genRandomUrls(List<String> uriPatterns) {
        final String pathVariableRegex = "\\{[^}]+\\}";
        final String additionalRegex1 = "/\\*\\*";
        final String additionalRegex2 = "/\\*";

        return uriPatterns.stream()
                .map(pat -> pat.replaceAll(pathVariableRegex,
                        String.valueOf(new Random().nextInt(0, 100))))
                .map(pat -> pat.replaceAll(additionalRegex1, "/random1"))
                .map(pat -> pat.replaceAll(additionalRegex2, "/random22"))
                .toList();
    }

    private PaletteException[] tokenRelatedExceptions() {
        return new PaletteException[]{
                ExpiredTokenException.EXCEPTION, InvalidTokenException.EXCEPTION,
                NoTokenExistsException.EXCEPTION, NoUserFoundViaTokenException.Exception,
                RoleMismatchException.EXCEPTION
        };
    }
}