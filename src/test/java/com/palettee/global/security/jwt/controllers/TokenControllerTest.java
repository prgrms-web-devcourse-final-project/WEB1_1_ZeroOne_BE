package com.palettee.global.security.jwt.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.palettee.global.exception.*;
import com.palettee.global.redis.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.servlet.http.*;
import jakarta.transaction.*;
import java.util.function.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
class TokenControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RedisService redisService;

    @Autowired
    UserRepository userRepo;

    private static User testUser;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        try {
            testUser = userRepo.save(
                    User.builder()
                            .email("test@test.com")
                            .build()
            );
        } catch (Exception e) {
            testUser = userRepo.findByEmail(testUser.getEmail()).orElseThrow();
        }
    }

    @Test
    @DisplayName("임시 토큰으로 필수 토큰을 발급")
    @WithAnonymousUser
    void issueToken() throws Exception {

        String temporaryToken = jwtUtils.createTemporaryToken(testUser);

        // status ok 하고 토큰 응답 잘 됐는지 확인
        MvcResult mvcResult = mvc.perform(get("/token/issue?token=" + temporaryToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        // 토큰 제대로 있는지 확인
        this.checkTokens(mvcResult);

        // 에러 발생 확인
        this.checkException(
                "/token/issue?token=", (url, token) -> get(url + token),
                jwtUtils::createTemporaryToken
        );
    }

    @Test
    @DisplayName("쿠키의 Refresh 토큰으로 재발급")
    @WithAnonymousUser
    void reissueToken() throws Exception {

        String refreshToken = jwtUtils.createRefreshToken(testUser);
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        redisService.storeRefreshToken(testUser, refreshToken, 10L);

        MvcResult mvcResult = mockMvc.perform(post("/token/reissue").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        // 토큰 제대로 있는지 확인
        this.checkTokens(mvcResult);

        // 새로운 refresh 토큰이 redis 에 저장됐는지 확인
        assertThat(redisService.getRefreshToken(testUser).orElseThrow())
                .isNotEqualTo(refreshToken);

        // 에러 발생 확인
        this.checkException(
                "/token/reissue",
                (url, token) -> post(url).cookie(new Cookie("refresh_token", token)),
                jwtUtils::createRefreshToken
        );
    }

    private void checkTokens(MvcResult mvcResult) {

        // 응답 헤더의 access 토큰 존재하는지 확인
        assertThat(mvcResult.getResponse().getHeader("Authorization"))
                .isNotEmpty();

        // 응답 쿠키에서 refresh 토큰 꺼냄
        Cookie cookie = mvcResult.getResponse().getCookie("refresh_token");
        assertThat(cookie).isNotNull();

        // redis 에 저장된 값과 동일한지 확인
        String refreshToken = cookie.getValue();
        assertThat(redisService.getRefreshToken(testUser))
                .isNotEmpty()
                .hasValue(refreshToken);
    }

    private void checkException(
            String url,
            BiFunction<String, String, RequestBuilder> requestFunction,
            Function<User, String> createNewTokenFunction
    ) throws Exception {

        // token 이 없을 때
        ErrorCode err = NoTokenExistsException.EXCEPTION.getErrorCode();

        mvc.perform(requestFunction.apply(url, ""))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // invalid 한 토큰이 주어졌을 때
        err = InvalidTokenException.EXCEPTION.getErrorCode();

        mvc.perform(requestFunction.apply(url, "RandomInvalidToken"))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // 토큰은 유효하지만 연관되는 유저가 없을 때
        err = NoUserFoundViaTokenException.Exception.getErrorCode();
        String newToken = createNewTokenFunction.apply(testUser);
        userRepo.delete(testUser);

        mvc.perform(requestFunction.apply(url, newToken))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));
    }
}