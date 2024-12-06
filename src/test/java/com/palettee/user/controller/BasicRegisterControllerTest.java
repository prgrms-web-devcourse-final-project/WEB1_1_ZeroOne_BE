package com.palettee.user.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.palettee.global.exception.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.repository.*;
import jakarta.transaction.*;
import java.util.*;
import java.util.function.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
class BasicRegisterControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepo;

    @Autowired
    ObjectMapper mapper;

    static User testUser;
    static User otherUser;
    static String ACCESS_TOKEN;

    // httpMethod, url 로 MockHttpServletRequestBuilder 만드는 편의용 개체
    final BiFunction<HttpMethod, String, MockHttpServletRequestBuilder> requestBuilder
            = (method, url) -> switch (method.name()) {
        case "GET" -> get(url);
        case "POST" -> post(url);
        default -> throw new IllegalStateException("Unexpected value: " + method.name());
    };

    @BeforeEach
    void setup() {
        testUser = userRepo.save(
                User.builder()
                        .email("test@test.com")
                        .name("test")
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );
        otherUser = userRepo.save(
                User.builder()
                        .email("test2@test.com")
                        .name("test2")
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );
        ACCESS_TOKEN = jwtUtils.createAccessToken(testUser);
    }

    @AfterEach
    void remove() {
        userRepo.deleteAll();
    }

    private RegisterBasicInfoRequest gen(String major, String minor, String div,
            List<String> urls) {
        return new RegisterBasicInfoRequest(
                "이름", "자기소개", "test-image-url.com", major, minor,
                "타이틀", div, urls, null
        );
    }

    @Test
    @DisplayName("유저 기본 정보 등록시 기초 정보 보여주기")
    void showBasicInfo() throws Exception {
        // 정상 작동 확인
        mvc.perform(get("/profile")
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(testUser.getEmail())))
                .andExpect(content().string(containsString(testUser.getName())))
                .andDo(print());

        logHappyFlow();

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.GET, "/profile");
    }

    @Test
    @DisplayName("유저 기본 정보 등록하기")
    void registerBasicInfo() throws Exception {

        // 정상 요청 들어왔을 때
        RegisterBasicInfoRequest request = gen("developer", "backend", "student",
                List.of("111.com", "222.com", "333.com"));
        String body = mapper.writeValueAsString(request);

        mvc.perform(post("/profile")
                        .header("Authorization", ACCESS_TOKEN)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        logHappyFlow();

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.POST, "/profile");

        // majorJobGroup, minorJobGroup 잘못 주어졌을 때
        var invalidGroup1 = gen("!!이상한거!!", "backend", "student", null);
        var invalidGroup2 = gen("etc", "!!이상한거!!", "student", null);

        this.checkValidationException("/profile", invalidGroup1, invalidGroup2);

        // major, minor 그룹 잘못 이어어져 있을 때
        var jobGroupMismatch = gen("etc", "backend", "student", null);
        // division 잘못 주어졌을 때
        var divisionMismatch = gen("developer", "backend", "!!!이상한거!!!", null);

        this.checkValidationException("/profile", jobGroupMismatch, divisionMismatch);

        // socials 5 개 초과 주어졌을 때
        var tooManyUrl = gen("developer", "backend", "student",
                List.of("111.com", "222.com", "333.com", "444.com", "555.com", "666.com"));

        this.checkValidationException("/profile", tooManyUrl);
    }

    @Test
    @DisplayName("유저 포폴 정보 등록하기")
    void registerPortfolio() throws Exception {

        RegisterPortfolioRequest request = new RegisterPortfolioRequest("Port8080.com");
        String body = mapper.writeValueAsString(request);

        mvc.perform(post("/portfolio")
                        .header("Authorization", ACCESS_TOKEN)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        logHappyFlow();

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.POST, "/portfolio");

        // request body 유효성 확인
        RegisterPortfolioRequest invalidRequest = new RegisterPortfolioRequest(
                "Very long link".repeat(200)
        );

        this.checkValidationException("/portfolio", invalidRequest);
    }

    private void checkJwtException(HttpMethod method, String url) throws Exception {

        // 토큰 없을 때
        ErrorCode err = NoTokenExistsException.EXCEPTION.getErrorCode();
        mvc.perform(requestBuilder.apply(method, url))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // 토큰 이상할 때
        err = InvalidTokenException.EXCEPTION.getErrorCode();
        mvc.perform(requestBuilder.apply(method, url)
                        .header("Authorization", "RandomInvalidToken"))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // 토큰으로 유저 못찾을 때
        err = NoUserFoundViaTokenException.Exception.getErrorCode();
        String token = jwtUtils.createAccessToken(otherUser);
        userRepo.delete(otherUser);

        mvc.perform(requestBuilder.apply(method, url)
                        .header("Authorization", token))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        otherUser = userRepo.save(otherUser);

        log.info("All exceptions were covered");
    }

    private void checkValidationException(String url, Object... bodies) throws Exception {
        for (Object body : bodies) {
            String content = mapper.writeValueAsString(body);

            mvc.perform(post(url)
                            .header("Authorization", ACCESS_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(400));
        }
    }

    private static void logHappyFlow() {
        log.info("Happy flow covered");
    }
}