package com.palettee.user.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.palettee.global.exception.*;
import com.palettee.global.security.jwt.exceptions.*;
import com.palettee.global.security.jwt.utils.*;
import com.palettee.user.controller.dto.request.reports.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import com.palettee.user.service.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class ReportControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepo;

    @Autowired
    private ReportService reportService;

    @Autowired
    ReportRepository reportRepo;

    @Autowired
    ReportCommentRepository reportCommentRepo;

    @Autowired
    ObjectMapper mapper;

    static User testUser;
    static User otherUser;
    static String ACCESS_TOKEN;
    final static int TEST_SIZE = 10;

    final BiFunction<HttpMethod, String, MockHttpServletRequestBuilder> requestBuilder
            = (method, url) -> switch (method.name()) {
        case "GET" -> get(url);
        case "POST" -> post(url);
        case "PATCH" -> patch(url);
        default -> throw new IllegalStateException("Unexpected value: " + method.name());
    };

    private RegisterReportRequest genRequest(String title, ReportType type) {
        return new RegisterReportRequest(title, "내용", type.toString());
    }

    private List<RegisterReportRequest> genRequestList(String title, ReportType type) {
        return IntStream.range(0, TEST_SIZE).boxed()
                .map(i -> this.genRequest(title + i, type))
                .toList();
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
                        .userRole(UserRole.ADMIN)
                        .build()
        );
        otherUser = userRepo.save(
                User.builder()
                        .name("test2")
                        .email("test2@test.com")
                        .briefIntro("자기소개")
                        .imageUrl("프로필 이미지")
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .jobTitle("잡 타이틀")
                        .division(Division.STUDENT)
                        .userRole(UserRole.USER)
                        .build()
        );
        ACCESS_TOKEN = jwtUtils.createAccessToken(testUser);
    }

    @AfterEach
    void remove() {
        reportCommentRepo.deleteAll();
        reportRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("새로운 제보를 등록")
    void registerReport() throws Exception {

        var request = new RegisterReportRequest("제목", "내용", "other");
        String body = mapper.writeValueAsString(request);

        // 정상 응답 확인
        mvc.perform(post("/report")
                        .header("Authorization", ACCESS_TOKEN)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId", is(notNullValue())));

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.POST, "/report");

        // validation 에러 확인
        var emptyTitleReq = new RegisterReportRequest("    ", "내용", "other");   // 빈 제목
        var emptyContentReq = new RegisterReportRequest("제목", "    ", "other"); // 빈 내용
        // reportType 이상한거
        var invalidReportTypeReq = new RegisterReportRequest("제목", "내용", "!!!이상한거!!!");

        this.checkValidationException(HttpMethod.POST, "/report",
                emptyTitleReq, emptyContentReq, invalidReportTypeReq);
    }

    @Test
    @DisplayName("제보에 새로운 댓글을 등록")
    void registerReportComment() throws Exception {
        Long reportId = reportService.registerReport(
                new RegisterReportRequest("제목", "내용", "other"), testUser
        ).reportId();

        var request = new RegisterReportCommentRequest("댓글 내용");
        String body = mapper.writeValueAsString(request);

        // 정상 응답 확인
        mvc.perform(post("/report/" + reportId + "/comment")
                        .header("Authorization", ACCESS_TOKEN)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportCommentId", is(notNullValue())));

        // reportId 없을때 에러 확인
        ErrorCode err = ReportNotFoundException.EXCEPTION.getErrorCode();
        mvc.perform(post("/report/" + Long.MAX_VALUE + "/comment")
                        .header("Authorization", ACCESS_TOKEN)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.POST, "/report/" + reportId + "/comment");

        // validation 에러 확인
        this.checkValidationException(HttpMethod.POST, "/report/" + reportId + "/comment",
                new RegisterReportCommentRequest("    ")    // 빈 댓글
        );
    }

    @Test
    @WithAnonymousUser
    @DisplayName("제보 목록 보여주기")
    void getReports() throws Exception {

        // 기본 정보들 입력
        var bugReportReqs = this.genRequestList("bug", ReportType.BUG);
        var enhanceReportReqs = this.genRequestList("bug", ReportType.ENHANCEMENT);
        var otherReportReqs = this.genRequestList("bug", ReportType.OTHER);

        List<Long> ids = new ArrayList<>(
                bugReportReqs.size() + enhanceReportReqs.size() + otherReportReqs.size()
        );
        for (var reqs : new List[]{bugReportReqs, enhanceReportReqs, otherReportReqs}) {
            for (var req : reqs) {
                ids.add(
                        reportService.registerReport((RegisterReportRequest) req, testUser)
                                .reportId()
                );
            }
        }

        List<Report> allReports = ids.stream()
                .map(reportRepo::findById)
                .map(Optional::orElseThrow)
                .toList();

        int bugReportCount = bugReportReqs.size();
        int enhanceReportCount = bugReportReqs.size();
        int otherReportCount = bugReportReqs.size();

        // 정상 응답 확인 : 각 report 의 절반 가져오니까 hasNext true
        mvc.perform(get("/report?type=all&page=0&size=" + allReports.size() / 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(allReports.size() / 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(true)))
                .andExpect(jsonPath("$.data.reports.length()", is(allReports.size() / 2)));
        mvc.perform(get("/report?type=bug&page=0&size=" + bugReportCount / 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(bugReportCount / 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(true)))
                .andExpect(jsonPath("$.data.reports.length()", is(bugReportCount / 2)));
        mvc.perform(get("/report?type=enhance&page=0&size=" + enhanceReportCount / 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(enhanceReportCount / 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(true)))
                .andExpect(jsonPath("$.data.reports.length()", is(enhanceReportCount / 2)));
        mvc.perform(get("/report?type=other&page=0&size=" + otherReportCount / 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(otherReportCount / 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(true)))
                .andExpect(jsonPath("$.data.reports.length()", is(otherReportCount / 2)));

        // 정상 응답 확인 : 각 report + 2 개 가져오니까 hasNext false
        mvc.perform(get("/report?type=all&page=0&size=" + (allReports.size() + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(allReports.size() + 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(false)))
                .andExpect(jsonPath("$.data.reports.length()", is(allReports.size())));
        mvc.perform(get("/report?type=bug&page=0&size=" + (bugReportCount + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(bugReportCount + 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(false)))
                .andExpect(jsonPath("$.data.reports.length()", is(bugReportCount)));
        mvc.perform(get("/report?type=enhance&page=0&size=" + (enhanceReportCount + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(enhanceReportCount + 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(false)))
                .andExpect(jsonPath("$.data.reports.length()", is(enhanceReportCount)));
        mvc.perform(get("/report?type=other&page=0&size=" + (otherReportCount + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slice.currentPage", is(0)))
                .andExpect(jsonPath("$.data.slice.size", is(otherReportCount + 2)))
                .andExpect(jsonPath("$.data.slice.hasNext", is(false)))
                .andExpect(jsonPath("$.data.reports.length()", is(otherReportCount)));

    }

    @Test
    @WithAnonymousUser
    @DisplayName("등록된 제보 세부 내용을 조회")
    void getReportDetail() throws Exception {
        Long reportId = reportService.registerReport(
                new RegisterReportRequest("제목", "내용", "other"), testUser
        ).reportId();

        var comments = IntStream.range(0, TEST_SIZE).boxed()
                .map(i -> new RegisterReportCommentRequest("댓글" + i))
                .toList();
        for (var comment : comments) {
            reportService.registerComment(reportId, comment, testUser);
        }

        Report report = reportRepo.findById(reportId).orElseThrow();

        // 정상 응답 확인
        mvc.perform(get("/report/" + reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId", is(
                        Integer.parseInt(String.valueOf(reportId))
                )))
                .andExpect(jsonPath("$.data.title", is(report.getTitle())))
                .andExpect(jsonPath("$.data.content", is(report.getContent())))
                .andExpect(jsonPath("$.data.reportType", is(
                        report.getReportType().toString()
                )))
                .andExpect(jsonPath("$.data.isFixed", is(report.isFixed())))
                .andExpect(jsonPath("$.data.reportDate", is(
                        report.getCreateAt().toString()
                )))
                .andExpect(jsonPath("$.data.userId", is(
                        Integer.parseInt(String.valueOf(testUser.getId()))
                )))
                .andExpect(jsonPath("$.data.userName", is(testUser.getName())))
                .andExpect(jsonPath("$.data.reportComments.length()", is(comments.size())));

        // reportId 없을때 에러 확인
        ErrorCode err = ReportNotFoundException.EXCEPTION.getErrorCode();
        mvc.perform(get("/report/" + Long.MAX_VALUE))
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));
    }

    @Test
    @DisplayName("특정 제보를 해결함으로 변경")
    void fixReport() throws Exception {
        Long reportId = reportService.registerReport(
                new RegisterReportRequest("제목", "내용", "other"), testUser
        ).reportId();

        // 정상 응답 확인
        mvc.perform(patch("/report/" + reportId + "/fixed")
                        .header("Authorization", ACCESS_TOKEN))     // ADMIN 계정 토큰임
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId", is(notNullValue())));

        // 권한 안되는 유저가 시도시 에러 확인
        ErrorCode err = RoleMismatchException.EXCEPTION.getErrorCode();

        String accessToken = jwtUtils.createAccessToken(otherUser);
        mvc.perform(patch("/report/" + reportId + "/fixed")
                        .header("Authorization", accessToken))      // USER 계정 토큰임
                .andExpect(status().is(err.getStatus()))
                .andExpect(content().string(containsString(err.getReason())));

        // jwt 관련 에러 확인
        this.checkJwtException(HttpMethod.PATCH, "/report/" + reportId + "/fixed");
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

        log.info("All jwt exceptions were covered");
    }

    private void checkValidationException(HttpMethod method, String url, Object... bodies)
            throws Exception {

        for (Object body : bodies) {
            String content = mapper.writeValueAsString(body);

            mvc.perform(requestBuilder.apply(method, url)
                            .header("Authorization", ACCESS_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(400));
        }

        log.info("All validation exceptions were covered");
    }


}