package com.palettee.user.service;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.controller.dto.response.*;
import com.palettee.user.controller.dto.request.reports.*;
import com.palettee.user.controller.dto.response.reports.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.assertj.core.data.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;

@Slf4j
@SpringBootTest
class ReportServiceTest {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportRepository reportRepo;

    @Autowired
    ReportCommentRepository reportCommentRepo;

    @Autowired
    UserRepository userRepo;

    static User testUser;
    final static int TEST_SIZE = 20;
    final static Comparator<Report> oldest = Comparator.comparing(Report::getCreateAt)
            .thenComparing(Report::getId);
    final static Comparator<Report> latest = oldest.reversed();

    private RegisterReportRequest genReportRequest(String title, ReportType type) {
        return new RegisterReportRequest(
                title, "내용", type.toString()
        );
    }

    private RegisterReportCommentRequest genReportCommentRequest(String content) {
        return new RegisterReportCommentRequest(content);
    }

    private Report genReport(String title, ReportType type, boolean isFixed) {
        return Report.builder()
                .title(title)
                .content("내용")
                .isFixed(isFixed)
                .reportType(type)
                .user(testUser)
                .build();
    }

    private List<Report> genReportList(String title, ReportType type, boolean isFixed) {
        return IntStream.range(0, TEST_SIZE).boxed()
                .map(i -> this.genReport(title + i, type, isFixed))
                .map(e -> reportRepo.save(e))
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
                        .userRole(UserRole.REAL_NEWBIE)
                        .build()
        );
    }

    @AfterEach
    void remove() {
        reportCommentRepo.deleteAll();
        reportRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("새로운 제보를 등록")
    void registerReport() {
        log.info("<-- registerReport");

        ReportType type = ReportType.BUG;
        var request = this.genReportRequest("제목", type);

        var result = reportService.registerReport(request, testUser);
        Report newReport = reportRepo.findByIdFetchWithComments(result.reportId())
                .orElseThrow();

        // 반환값 확인
        assertThat(result).satisfies(
                r -> assertThat(r).isNotNull(),
                r -> assertThat(r.reportId()).isEqualTo(newReport.getId())
        );

        // db 에 제대로 등록 됬는지 확인
        assertThat(newReport).satisfies(
                e -> assertThat(e.getTitle()).isEqualTo(request.title()),
                e -> assertThat(e.getContent()).isEqualTo(request.content()),
                e -> assertThat(e.getReportType()).isEqualTo(type),
                e -> assertThat(e.getReportComments()).isEmpty(),
                e -> assertThat(e.getUser().getId()).isEqualTo(testUser.getId())
        );

        // 예외 확인
        assertThatThrownBy(() -> reportService.registerReport(
                new RegisterReportRequest("제목", "내용", "!!!!이상한거!!!!"),
                testUser
        )).isInstanceOf(InvalidReportTypeException.class);

        log.info("--> registerReport");
    }

    @Test
    @DisplayName("제보에 새로운 댓글을 등록")
    void registerComment() {
        log.info("<-- registerComment");

        // 테스트용 report 등록
        Long reportId = reportService.registerReport(
                this.genReportRequest("제목", ReportType.BUG), testUser
        ).reportId();

        var request = this.genReportCommentRequest("댓글 내용");
        var result = reportService.registerComment(
                reportId, request, testUser
        );
        ReportComment newComment = reportCommentRepo.findById(result.reportCommentId())
                .orElseThrow();

        // 반환값 확인
        assertThat(result).satisfies(
                r -> assertThat(r).isNotNull(),
                r -> assertThat(r.reportCommentId()).isEqualTo(newComment.getId())
        );

        // db 에 제대로 등록 됬는지 확인
        assertThat(newComment).satisfies(
                e -> assertThat(e.getContent()).isEqualTo(request.content()),
                e -> assertThat(e.getReport().getId()).isEqualTo(reportId),
                e -> assertThat(e.getUser().getId()).isEqualTo(testUser.getId())
        );

        // 예외 확인
        assertThatThrownBy(() -> reportService.registerComment(Long.MAX_VALUE, request, testUser))
                .isInstanceOf(ReportNotFoundException.class);

        log.info("--> registerComment");
    }

    @Test
    @DisplayName("제보 목록 보여주기")
    void getReports() {
        log.info("<-- getReports");

        // 데이터 집어넣기
        List<Report> bugReports = new ArrayList<>(2 * TEST_SIZE);
        bugReports.addAll(this.genReportList("bug-unfix", ReportType.BUG, false));
        bugReports.addAll(this.genReportList("bug-fix", ReportType.BUG, true));

        List<Report> otherReports = new ArrayList<>(2 * TEST_SIZE);
        otherReports.addAll(this.genReportList("other-unfix", ReportType.OTHER, false));
        otherReports.addAll(this.genReportList("other-fix", ReportType.OTHER, true));

        List<Report> fixedEnhanceReports = new ArrayList<>(2 * TEST_SIZE);
        fixedEnhanceReports.addAll(
                this.genReportList("enhance-unfix", ReportType.ENHANCEMENT, false));
        fixedEnhanceReports.addAll(this.genReportList("enhance-fix", ReportType.ENHANCEMENT, true));

        List<Report> allReports = new ArrayList<>(
                bugReports.size() + otherReports.size() + fixedEnhanceReports.size()
        );
        allReports.addAll(bugReports);
        allReports.addAll(otherReports);
        allReports.addAll(fixedEnhanceReports);

        // 안 고쳐진 모든 report 최신순으로 확인
        List<Report> expected = allReports.stream()
                .filter(e -> !e.isFixed())
                .sorted(latest)
                .toList();
        this.checkPageResult(5, "latest", null, "unfixed", expected, latest);

        // 고침 상관 X, bug report 오래된순으로 확인
        expected = allReports.stream()
                .filter(r -> r.getReportType().equals(ReportType.BUG))
                .sorted(oldest)
                .toList();
        this.checkPageResult(8, "oldest", "bug", null, expected, oldest);

        // 고쳐진 아무 report 최신순으로 확인
        expected = allReports.stream()
                .filter(Report::isFixed)
                .sorted(latest)
                .toList();
        this.checkPageResult(3, "latest", null, "fixed", expected, latest);

        log.info("--> getReports");
    }

    @Test
    @DisplayName("등록된 제보 세부 내용을 조회")
    void getReportDetail() {
        log.info("<-- getReportDetail");

        ReportType type = ReportType.BUG;
        var reportRequest = this.genReportRequest("제목", type);
        Long reportId = reportService.registerReport(reportRequest, testUser).reportId();

        var result = reportService.getReportDetail(reportId);

        // 조회 값 일치 확인
        assertThat(result).satisfies(
                r -> assertThat(r).isNotNull(),
                r -> assertThat(r.reportId()).isEqualTo(reportId),
                r -> assertThat(r.title()).isEqualTo(reportRequest.title()),
                r -> assertThat(r.content()).isEqualTo(reportRequest.content()),
                r -> assertThat(r.reportType()).isEqualTo(type),
                r -> assertThat(r.isFixed()).isFalse(),
                r -> assertThat(r.userId()).isEqualTo(testUser.getId()),
                r -> assertThat(r.userName()).isEqualTo(testUser.getName())
        );

        // 예외 확인
        assertThatThrownBy(() -> reportService.getReportDetail(Long.MAX_VALUE))
                .isInstanceOf(ReportNotFoundException.class);

        log.info("--> getReportDetail");
    }

    @Test
    @DisplayName("특정 제보의 댓글 내용을 조회")
    void getReportComments() {
        log.info("<-- getReportComments");

        Long reportId = reportService.registerReport(
                this.genReportRequest("제목", ReportType.BUG), testUser
        ).reportId();

        List<RegisterReportCommentRequest> commentRequests = IntStream.range(0, TEST_SIZE).boxed()
                .map(i -> this.genReportCommentRequest("댓글 내용" + i))
                .toList();

        List<ReportCommentResponse> commentResp = new ArrayList<>(commentRequests.size());

        for (var commentRequest : commentRequests) {
            commentResp.add(
                    reportService.registerComment(reportId, commentRequest, testUser)
            );
        }

        var reportComments = commentResp.stream()
                .map(ReportCommentResponse::reportCommentId)
                .map(reportCommentRepo::findById)
                .map(Optional::orElseThrow)
                .toList();

        final int PAGE_SIZE = TEST_SIZE / 3;

        for (int pageNum = 0; ; pageNum++) {

            var result = reportService.getReportComments(
                    reportId, PageRequest.of(pageNum, PAGE_SIZE)
            );

            List<ReportComment> expected = reportComments.subList(
                    pageNum * PAGE_SIZE,
                    Math.min(pageNum * PAGE_SIZE + PAGE_SIZE, reportComments.size())
            );

            var comments = result.comments();

            assertThat(comments).hasSize(expected.size());

            for (int i = 0; i < comments.size(); i++) {
                var comment = comments.get(i);
                var expectedComment = expected.get(i);

                assertThat(comment).isNotNull().satisfies(
                        r -> assertThat(r.reportCommentId()).isEqualTo(expectedComment.getId()),
                        r -> assertThat(r.content()).isEqualTo(expectedComment.getContent()),
                        r -> assertThat(r.userId()).isEqualTo(testUser.getId()),
                        r -> assertThat(r.userName()).isEqualTo(testUser.getName())
                );
            }

            SliceInfo slice = result.slice();

            assertThat(slice).isNotNull();
            assertThat(slice.size()).isEqualTo(PAGE_SIZE);

            if (!slice.hasNext()) {
                break;
            }
        }

        log.info("--> getReportComments");
    }

    @Test
    @DisplayName("특정 제보를 해결함으로 변경")
    void fixReport() {
        log.info("<-- fixReport");

        Long reportId = reportService.registerReport(
                this.genReportRequest("제목", ReportType.BUG), testUser
        ).reportId();

        // 응답 확인
        var result = reportService.fixReport(reportId, testUser);
        assertThat(result).isNotNull().satisfies(
                r -> assertThat(r.reportId()).isEqualTo(reportId)
        );

        // db 에 반영 됬는지 확인
        Report report = reportRepo.findById(reportId).orElseThrow();
        assertThat(report.isFixed()).isTrue();

        // 예외 확인
        assertThatThrownBy(() -> reportService.fixReport(Long.MAX_VALUE, testUser))
                .isInstanceOf(ReportNotFoundException.class);

        log.info("--> fixReport");
    }

    /**
     * @see ReportService#getReports
     */
    private void checkPageResult(
            int pageSize, String sort, String type, String include,
            List<Report> list, Comparator<Report> expectedOrder
    ) {

        for (int pageNum = 0; ; pageNum++) {
            Pageable pageable = PageRequest.of(pageNum, pageSize);

            var result = reportService.getReports(
                    sort, type, include, pageable
            );

            List<Report> expected = list.subList(
                    pageNum * pageSize,
                    Math.min(pageNum * pageSize + pageSize, list.size())
            );

            assertThat(expected).isSortedAccordingTo(expectedOrder);

            final int check = pageNum;
            assertThat(result).satisfies(
                    r -> assertThat(r).isNotNull(),
                    r -> assertThat(r.slice().currentPage()).isEqualTo(check),
                    r -> assertThat(r.slice().size()).isEqualTo(pageSize)
            );

            List<SimpleReportResponse> listFromResult = result.reports();
            assertThat(listFromResult).hasSize(expected.size());

            List<SimpleReportResponse> casted = expected.stream()
                    .map(SimpleReportResponse::of)
                    .toList();

            for (int i = 0; i < casted.size(); i++) {
                SimpleReportResponse res = listFromResult.get(i);
                SimpleReportResponse e = casted.get(i);

                assertThat(res).satisfies(
                        r -> assertThat(r.reportId()).isEqualTo(e.reportId()),
                        r -> assertThat(r.title()).isEqualTo(e.title()),
                        r -> assertThat(r.reportType()).isEqualTo(e.reportType()),
                        r -> assertThat(r.isFixed()).isEqualTo(e.isFixed()),
                        r -> assertThat(r.userId()).isEqualTo(e.userId()),
                        r -> assertThat(r.userName()).isEqualTo(e.userName())
                );

                LocalDateTime time1 = parse(res.reportedAt());
                LocalDateTime time2 = parse(e.reportedAt());

                assertThat(time1).isCloseTo(time2,
                        new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
            }

            if (!result.slice().hasNext()) {
                break;
            }
        }
    }

    private LocalDateTime parse(String time) {
        return LocalDateTime.parse(time);
    }
}