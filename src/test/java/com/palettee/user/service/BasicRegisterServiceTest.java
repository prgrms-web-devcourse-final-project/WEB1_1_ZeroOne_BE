package com.palettee.user.service;

import static org.assertj.core.api.Assertions.*;

import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.*;
import com.palettee.user.controller.dto.response.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@SpringBootTest
@Transactional
class BasicRegisterServiceTest {

    @Autowired
    UserRepository userRepo;
    @Autowired
    RelatedLinkRepository relatedLinkRepo;
    @Autowired
    PortFolioRepository portFolioRepo;

    @Autowired
    BasicRegisterService basicRegisterService;

    static User testUser;
    static final RegisterBasicInfoRequest registerBasicInfoRequest
            = new RegisterBasicInfoRequest(
            "이름", "자기소개",
            MajorJobGroup.ETC.toString(), MinorJobGroup.DELIVERY.toString(),
            "직무 타이틀", Division.WORKER.toString(),
            List.of("google.com", "github.com", "random.com")
    );

    @BeforeEach
    void setUp() {
        testUser = userRepo.save(
                User.builder()
                        .email("test@test.com")
                        .name("test")
                        .userRole(UserRole.REAL_NEWBIE)
                        .division(Division.ETC)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );
    }

    @AfterEach
    void remove() {
        userRepo.deleteAll();
        relatedLinkRepo.deleteAll();
        portFolioRepo.deleteAll();
    }

    @Test
    @DisplayName("유저 기본 정보 등록시 기초 정보 보여주기")
    void showBasicInfo() {

        BasicInfoResponse result = basicRegisterService.showBasicInfo(testUser);

        // 응답 검증
        checkResult(result);
    }

    @Test
    @DisplayName("유저 기본 정보 등록하기")
    void registerBasicInfo() {

        UserResponse result = basicRegisterService.registerBasicInfo(
                testUser, registerBasicInfoRequest
        );

        // 응답 검증
        checkResult(result);

        // 정보 진짜 변경 됐는지 확인
        User verify = userRepo.findById(testUser.getId()).orElseThrow();
        checkEquality(verify);

        checkExceptions(testUser);
    }

    @Test
    @DisplayName("유저 포폴 정보 등록하기")
    void registerPortfolio() {
        RegisterPortfolioRequest request = new RegisterPortfolioRequest("test.com");

        UserResponse result = basicRegisterService.registerPortfolio(testUser, request);

        // 응답 검증
        checkResult(result);

        // 정보 진짜 변경 됐는지 확인
        User verify = userRepo.findById(testUser.getId()).orElseThrow();
        assertThat(verify.getPortfolios()).hasSize(1);
        assertThat(verify.getPortfolios().get(0).getUrl()).isEqualTo(request.portfolioUrl());
    }

    private void checkResult(BasicInfoResponse result) {
        assertThat(result).isNotNull().satisfies(
                r -> assertThat(r.email()).isNotNull().isEqualTo(testUser.getEmail()),
                r -> assertThat(r.name()).isNotNull().isEqualTo(testUser.getName())
        );
    }

    private void checkResult(UserResponse result) {
        assertThat(result).isNotNull();
        assertThat(result.userId()).isNotNull().isEqualTo(testUser.getId());
    }

    private void checkEquality(User user) {
        // 요청대로 이름, 자기소개, 직무 타이틀 변경 됐는지 확인
        assertThat(user).satisfies(
                u -> assertThat(u.getName()).isEqualTo(
                        BasicRegisterServiceTest.registerBasicInfoRequest.name()),
                u -> assertThat(u.getBriefIntro()).isEqualTo(
                        BasicRegisterServiceTest.registerBasicInfoRequest.briefIntro()),
                u -> assertThat(u.getJobTitle()).isEqualTo(
                        BasicRegisterServiceTest.registerBasicInfoRequest.jobTitle())
        );

        MajorJobGroup majorGroup = MajorJobGroup.of(
                BasicRegisterServiceTest.registerBasicInfoRequest.majorJobGroup());
        MinorJobGroup minorGroup = MinorJobGroup.of(
                BasicRegisterServiceTest.registerBasicInfoRequest.minorJobGroup());

        // 직무도 변경 됐는지 확인
        assertThat(majorGroup).isEqualTo(user.getMajorJobGroup());
        assertThat(minorGroup).isEqualTo(user.getMinorJobGroup());

        // url 도 변경 됐는지 확인
        List<String> relatedLinks = user.getRelatedLinks()
                .stream()
                .map(RelatedLink::getLink)
                .sorted()
                .toList();
        List<String> requestLinks = BasicRegisterServiceTest.registerBasicInfoRequest.url()
                .stream()
                .sorted()
                .toList();

        assertThat(relatedLinks).hasSameElementsAs(requestLinks);
    }

    private void checkExceptions(User user) {
        // 직군 이상한거 제공 1
        RegisterBasicInfoRequest invalidRequest1 = new RegisterBasicInfoRequest(
                "이름", "자기소개", "!!!!이상한거!!!!", "backend",
                "타이틀", "student", null
        );

        // 직군 이상한거 제공 2
        RegisterBasicInfoRequest invalidRequest2 = new RegisterBasicInfoRequest(
                "이름", "자기소개", "ETC", "!!!!이상한거!!!!",
                "타이틀", "student", null
        );

        // 대직군 - 소직군 안맞음
        RegisterBasicInfoRequest invalidRequest3 = new RegisterBasicInfoRequest(
                "이름", "자기소개", "ETC", "backend",
                "타이틀", "student", null
        );

        RegisterBasicInfoRequest invalidRequest4 = new RegisterBasicInfoRequest(
                "이름", "자기소개", "DEVELOPER", "backend",
                "타이틀", "!!!!이상한거!!!!", null
        );

        assertThatThrownBy(() -> basicRegisterService.registerBasicInfo(user, invalidRequest1))
                .isInstanceOf(InvalidJobGroupException.class);

        assertThatThrownBy(() -> basicRegisterService.registerBasicInfo(user, invalidRequest2))
                .isInstanceOf(InvalidJobGroupException.class);

        assertThatThrownBy(() -> basicRegisterService.registerBasicInfo(user, invalidRequest3))
                .isInstanceOf(JobGroupMismatchException.class);

        assertThatThrownBy(() -> basicRegisterService.registerBasicInfo(user, invalidRequest4))
                .isInstanceOf(InvalidDivisionException.class);
    }
}