package com.palettee.user.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.palettee.archive.domain.*;
import com.palettee.archive.repository.*;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.*;
import com.palettee.global.exception.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.services.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import jakarta.servlet.http.*;
import jakarta.transaction.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.http.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepo;
    @Autowired
    PortFolioRepository portFolioRepo;
    @Autowired
    RelatedLinkRepository relatedLinkRepo;
    @Autowired
    ArchiveRepository archiveRepo;
    @Autowired
    GatheringRepository gatheringRepo;

    @Autowired
    ObjectMapper mapper;

    static User testUser;
    static User otherUser;
    static String ACCESS_TOKEN;

    static PortFolio testPortFolio;
    static List<RelatedLink> testRelatedLinks;
    static List<Archive> testArchives;
    static List<Gathering> testGatherings;

    final static int TEST_SIZE = 5;
    static String BASE_URL;

    final BiFunction<HttpMethod, String, MockHttpServletRequestBuilder> requestBuilder
            = (method, url) -> switch (method.name()) {
        case "GET" -> get(url);
        case "POST" -> post(url);
        default -> throw new IllegalStateException("Unexpected value: " + method.name());
    };
    @Autowired
    private RefreshTokenRedisService refreshTokenRedisService;


    private List<Archive> genArchiveList(int size, ArchiveType color, User user) {
        return IntStream.range(0, size).boxed()
                .map(i -> new Archive(
                        color.toString() + i, "desc", "introduction", color, false, user
                )).toList();
    }

    private List<Gathering> genGatheringListList(int size, User user) {
        return IntStream.range(0, size).boxed()
                .map(i -> new Gathering(
                        Sort.ETC, Subject.ETC, "period? 이게 뭐지?", Contact.OFFLINE,
                        LocalDateTime.MAX, 3, "test", "title" + i,
                        "content", user,null, null, null
                )).toList();
    }

    private EditUserInfoRequest genReq(String major, String minor, String div,
            List<String> urls) {
        return new EditUserInfoRequest(
                "이름바뀜", "자기소개바뀜", "test-image.com", major, minor,
                "타이틀바뀜", div, "포폴 url", urls, null
        );
    }

    @BeforeEach
    void setUp() {
        testUser = userRepo.save(
                User.builder()
                        .name("test")
                        .email("test@test.com")
                        .briefIntro("자기소개")
                        .imageUrl("프로필 이미지")
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .jobTitle("잡 타이틀")
                        .division(Division.STUDENT)
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );
        testPortFolio = portFolioRepo.save(new PortFolio(testUser, "portfolioLink.com"));
        testRelatedLinks = relatedLinkRepo.saveAll(List.of(
                new RelatedLink("github", testUser),
                new RelatedLink("blog", testUser)
        ));

        testArchives = genArchiveList(TEST_SIZE, ArchiveType.BLUE, testUser);
        testGatherings = genGatheringListList(TEST_SIZE, testUser);

        archiveRepo.saveAll(testArchives);
        gatheringRepo.saveAll(testGatherings);

        otherUser = userRepo.save(
                User.builder()
                        .email("test2@test.com")
                        .name("test2")
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );
        ACCESS_TOKEN = jwtUtils.createAccessToken(testUser);
        BASE_URL = "/user/" + testUser.getId();
    }

    @AfterEach
    void remove() {
        for (JpaRepository<?, ?> repo : new JpaRepository[]{
                userRepo, relatedLinkRepo, portFolioRepo,
                archiveRepo, gatheringRepo
        }) {
            repo.deleteAll();
        }
    }

    @Test
    @DisplayName("자기 자신의 정보를 조회")
    void getMyInfo() throws Exception {

        // 정상 작동 확인
        mvc.perform(get("/user/my-info")
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", is(
                        Integer.parseInt(String.valueOf(testUser.getId()))
                )))
                .andExpect(jsonPath("$.data.name", is(testUser.getName())))
                .andExpect(jsonPath("$.data.imageUrl", is(testUser.getImageUrl())))
                .andExpect(jsonPath("$.data.role", is(
                        testUser.getUserRole().toString()
                )));

        // jwt 에러 확인
        this.checkJwtException(HttpMethod.GET, BASE_URL + "/my-info");
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {

        String refreshToken = jwtUtils.createRefreshToken(testUser);
        refreshTokenRedisService.storeRefreshToken(testUser, refreshToken, 2);

        // 정상 작동 확인
        MvcResult result = mvc.perform(post("/user/logout")
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", is(
                        Integer.parseInt(String.valueOf(testUser.getId()))
                )))
                .andReturn();

        Cookie refreshTokenCookie = result.getResponse().getCookie("refresh_token");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isNull();

        // jwt 에러 확인
        this.checkJwtException(HttpMethod.POST, BASE_URL + "/logout");
    }

    @Test
    @WithAnonymousUser
    @DisplayName("특정 유저의 프로필을 확인")
    void getUserDetail() throws Exception {
        // 정상 작동 확인
        mvc.perform(get(BASE_URL + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is(testUser.getName())))
                .andExpect(jsonPath("$.data.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.data.briefIntro", is(testUser.getBriefIntro())))
                .andExpect(jsonPath("$.data.imageUrl", is(testUser.getImageUrl())))
                .andExpect(jsonPath("$.data.majorJobGroup",
                        is(testUser.getMajorJobGroup().toString())))
                .andExpect(jsonPath("$.data.minorJobGroup",
                        is(testUser.getMinorJobGroup().toString())))
                .andExpect(jsonPath("$.data.jobTitle", is(testUser.getJobTitle())))
                .andExpect(jsonPath("$.data.division",
                        is(testUser.getDivision().toString())))
                .andExpect(jsonPath("$.data.role", is(emptyOrNullString())))
                .andExpect(jsonPath("$.data.portfolioLink", is(testPortFolio.getUrl())))
                .andExpect(jsonPath("$.data.color",
                        is(ArchiveType.BLUE.toString())))
                .andExpect(jsonPath("$.data.socials.length()", is(testRelatedLinks.size())));

        // 없는 유저 조회하려 할 때
        ErrorCode err = UserNotFoundException.EXCEPTION.getErrorCode();
        mvc.perform(get("/user/" + Long.MAX_VALUE + "/profile"))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));
    }

    @Test
    @DisplayName("정보 수정 폼 불러오기")
    void getUserEditForm() throws Exception {
        // 정상 작동 확인
        mvc.perform(get(BASE_URL + "/edit")
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is(testUser.getName())))
                .andExpect(jsonPath("$.data.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.data.briefIntro", is(testUser.getBriefIntro())))
                .andExpect(jsonPath("$.data.imageUrl", is(testUser.getImageUrl())))
                .andExpect(jsonPath("$.data.majorJobGroup",
                        is(testUser.getMajorJobGroup().toString())))
                .andExpect(jsonPath("$.data.minorJobGroup",
                        is(testUser.getMinorJobGroup().toString())))
                .andExpect(jsonPath("$.data.jobTitle", is(testUser.getJobTitle())))
                .andExpect(jsonPath("$.data.division",
                        is(testUser.getDivision().toString())))
                .andExpect(jsonPath("$.data.portfolioLink", is(testPortFolio.getUrl())))
                .andExpect(jsonPath("$.data.socials.length()", is(testRelatedLinks.size())));

        // 없는 유저 조회하려 할 때
        ErrorCode err = UserNotFoundException.EXCEPTION.getErrorCode();
        mvc.perform(get("/user/" + Long.MAX_VALUE + "/edit")
                        .header("Authorization", ACCESS_TOKEN))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // jwt 에러 확인
        this.checkJwtException(HttpMethod.GET, BASE_URL + "/edit");
    }

    @Test
    @DisplayName("유저의 정보를 수정")
    void editUserInfo() throws Exception {

        EditUserInfoRequest request = genReq("etc", "etc", "student", List.of("11", "22"));
        String body = mapper.writeValueAsString(request);

        mvc.perform(put(BASE_URL + "/edit")
                        .header("Authorization", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId", is(
                        Integer.parseInt(String.valueOf(testUser.getId()))
                )));

        // 없는 유저 조회하려 할 때
        ErrorCode err = UserNotFoundException.EXCEPTION.getErrorCode();
        mvc.perform(put("/user/" + Long.MAX_VALUE + "/edit")
                        .header("Authorization", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // jwt 에러 확인
        this.checkJwtException(HttpMethod.POST, BASE_URL + "/edit");

        // majorJobGroup, minorJobGroup 잘못 주어졌을 때
        var invalidGroup1 = genReq("!!이상한거!!", "backend", "student", null);
        var invalidGroup2 = genReq("etc", "!!이상한거!!", "student", null);

        this.checkValidationException(BASE_URL + "/edit", invalidGroup1, invalidGroup2);

        // major, minor 그룹 잘못 이어어져 있을 때
        var jobGroupMismatch = genReq("etc", "backend", "student", null);
        // division 잘못 주어졌을 때
        var divisionMismatch = genReq("developer", "backend", "!!!이상한거!!!", null);

        this.checkValidationException(BASE_URL + "/edit", jobGroupMismatch, divisionMismatch);

        // url 5 개 초과 주어졌을 때
        var tooManyUrl = genReq("developer", "backend", "student",
                List.of("111.com", "222.com", "333.com", "444.com", "555.com", "666.com"));

        this.checkValidationException(BASE_URL + "/edit", tooManyUrl);

    }

    @Test
    @DisplayName("유저가 소유한 아카이브 목록을 조회")
    void getUserArchives() throws Exception {

        // 전체 아카이브 개수 = TEST_SIZE
        Long lastArchiveId = testArchives.stream()
                .sorted(Comparator.comparing(Archive::getId).reversed())
                .toList()
                .get(TEST_SIZE - 1)
                .getId();

        // TEST_SIZE - 1 개 가져왔으니까 nextArchiveId 는 마지막 원소 id 이어야 함.
        mvc.perform(get(BASE_URL + "/archives?size=" + (TEST_SIZE - 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext", is(true)))
                .andExpect(jsonPath("$.data.nextArchiveId", is(
                        Integer.parseInt(lastArchiveId.toString())
                )))
                .andExpect(jsonPath("$.data.archives.length()", is(TEST_SIZE - 1)));

        // lastArchiveId 로 NoOffset 하니까 가져오는 목록은 1 개, 다음 없어야 함.
        mvc.perform(get(BASE_URL + "/archives?size=" + TEST_SIZE +
                        "&nextArchiveId=" + lastArchiveId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.nextArchiveId", is(emptyOrNullString())))
                .andExpect(jsonPath("$.data.archives.length()", is(1)));

        // 없는 유저 id 로 가져오면 빈 목록 줘야 함.
        mvc.perform(get("/user/" + Long.MAX_VALUE + "/archives?size=" + TEST_SIZE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.nextArchiveId", is(emptyOrNullString())))
                .andExpect(jsonPath("$.data.archives.length()", is(0)));
    }

    @Test
    @DisplayName("유저가 소유한 소모임 목록을 조회")
    void getUserGatherings() throws Exception {

        // 전체 게더링 개수 = TEST_SIZE
        Long lastGatheringId = testGatherings.stream()
                .sorted(Comparator.comparing(Gathering::getId).reversed())
                .toList()
                .get(TEST_SIZE - 1)
                .getId();

        // TEST_SIZE - 1 개 가져왔으니까 nextGatheringId 는 마지막 원소 id 이어야 함.
        mvc.perform(get(BASE_URL + "/gatherings?size=" + (TEST_SIZE - 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext", is(true)))
                .andExpect(jsonPath("$.data.nextGatheringId", is(
                        Integer.parseInt(lastGatheringId.toString())
                )))
                .andExpect(jsonPath("$.data.gatherings.length()", is(TEST_SIZE - 1)));

        // lastArchiveId 로 NoOffset 하니까 가져오는 목록은 1 개, 다음 없어야 함.
        mvc.perform(get(BASE_URL + "/gatherings?size=" + TEST_SIZE +
                        "&nextGatheringId=" + lastGatheringId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.nextGatheringId", is(emptyOrNullString())))
                .andExpect(jsonPath("$.data.gatherings.length()", is(1)));
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

            mvc.perform(put(url)
                            .header("Authorization", ACCESS_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(400));
        }
    }
}