package com.palettee.user.service;

import static org.assertj.core.api.Assertions.*;

import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.controller.dto.response.users.*;
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
    StoredProfileImageUrlRepository storedProfileImageUrlRepo;

    @Autowired
    BasicRegisterService basicRegisterService;

    static User testUser;
    static final RegisterBasicInfoRequest registerBasicInfoRequest
            = gen(MajorJobGroup.DEVELOPER.toString(),
            MinorJobGroup.BACKEND.toString(),
            Division.STUDENT.toString(),
            List.of("111.com", "222.com", "333.com"),
            List.of("resource1.com", "resource2.com", "resource3.com")
    );

    private static RegisterBasicInfoRequest gen(String major, String minor,
            String div, List<String> urls, List<String> s3Resources) {
        return new RegisterBasicInfoRequest(
                "이름", "자기소개", "test-image-url.com", major, minor,
                "타이틀", div, urls, s3Resources
        );
    }

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
        storedProfileImageUrlRepo.deleteAll();
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

        // S3 저장된 자원들도 db 에 기록 됬는지 확인
        List<String> s3Resources = registerBasicInfoRequest.s3StoredImageUrls()
                .stream()
                .sorted()
                .toList();
        List<String> urls = storedProfileImageUrlRepo.findAllByUserId(testUser.getId())
                .stream().map(StoredProfileImageUrl::getUrl)
                .sorted()
                .toList();

        assertThat(urls).containsAll(s3Resources);

        checkExceptions(testUser);
    }

    @Test
    @DisplayName("유저 포폴 정보 등록하기")
    void registerPortfolio() {
        RegisterPortfolioRequest request = new RegisterPortfolioRequest("test.com");

        UserSavePortFolioResponse userSavePortFolioResponse = basicRegisterService.registerPortfolio(testUser, request);

        // 응답 검증
        checkResult(userSavePortFolioResponse);

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

    private void checkResult(UserSavePortFolioResponse result) {
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

        // socials 도 변경 됐는지 확인
        List<String> relatedLinks = user.getRelatedLinks()
                .stream()
                .map(RelatedLink::getLink)
                .sorted()
                .toList();
        List<String> requestLinks = BasicRegisterServiceTest.registerBasicInfoRequest.socials()
                .stream()
                .sorted()
                .toList();

        assertThat(relatedLinks).hasSameElementsAs(requestLinks);
    }

    private void checkExceptions(User user) {
        // 직군 이상한거 제공 1
        RegisterBasicInfoRequest invalidRequest1
                = gen("!!!!이상한거!!!!", "backend", "student", null
                , null);

        // 직군 이상한거 제공 2
        RegisterBasicInfoRequest invalidRequest2
                = gen("etc", "!!!!이상한거!!!!", "student",
                null, null);

        // 대직군 - 소직군 안맞음
        RegisterBasicInfoRequest invalidRequest3
                = gen("etc", "backend", "student",
                null, null);

        // 소속 이상한거
        RegisterBasicInfoRequest invalidRequest4
                = gen("developer", "backend", "!!!!이상한거!!!!",
                null, null);

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