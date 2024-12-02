package com.palettee.user.service;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.domain.*;
import com.palettee.archive.repository.*;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.*;
import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.*;
import com.palettee.user.controller.dto.response.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserRepository userRepo;

    @Autowired
    RelatedLinkRepository relatedLinkRepo;

    @Autowired
    PortFolioRepository portFolioRepo;

    @Autowired
    StoredProfileImageUrlRepository storedProfileImageUrlRepo;

    @Autowired
    ArchiveRepository archiveRepo;

    @Autowired
    ArchiveImageRepository archiveImageRepo;

    @Autowired
    GatheringRepository gatheringRepo;

    @Autowired
    UserService userService;

    static User testUser;

    static ArchiveType representativeColor;
    static List<Archive> blueArchives, redArchives, noColorArchives;
    final static int BLUE_ARCHIVE_SIZE = 6;
    final static int RED_ARCHIVE_SIZE = 4;
    final static int NO_COLOR_ARCHIVE_SIZE = 10;
    final static int TEST_SIZE = BLUE_ARCHIVE_SIZE + RED_ARCHIVE_SIZE + NO_COLOR_ARCHIVE_SIZE;

    static List<Gathering> testGatherings;

    private List<Archive> genArchiveList(int size, ArchiveType color, User user) {
        return IntStream.range(0, size).boxed()
                .map(i -> new Archive(
                        color.toString() + i, "desc", "introduction", color, false, user
                )).toList();
    }

    private Gathering genGathering(String title, User user) {
        return new Gathering(
                Sort.ETC, Subject.ETC, "period? 이게 뭐지?", Contact.OFFLINE,
                LocalDateTime.MAX, 3, Position.DEVELOP, title,
                "content", "url", user, null, null
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
        portFolioRepo.save(new PortFolio(testUser, "portfolioLink.com"));
        relatedLinkRepo.saveAll(List.of(
                new RelatedLink("github", testUser),
                new RelatedLink("blog", testUser)
        ));

        // 테스트용 아카이브 등록
        blueArchives = genArchiveList(BLUE_ARCHIVE_SIZE, ArchiveType.BLUE, testUser);
        redArchives = genArchiveList(RED_ARCHIVE_SIZE, ArchiveType.RED, testUser);
        noColorArchives = genArchiveList(NO_COLOR_ARCHIVE_SIZE, ArchiveType.NO_COLOR, testUser);

        blueArchives.forEach(a -> new ArchiveImage("blue.com", a));
        redArchives.forEach(a -> new ArchiveImage("red.com", a));

        archiveRepo.saveAll(blueArchives);
        archiveRepo.saveAll(redArchives);
        archiveRepo.saveAll(noColorArchives);

        representativeColor = blueArchives.size() > redArchives.size() ?
                ArchiveType.BLUE : ArchiveType.RED;

        // 테스트용 게더링 등록
        testGatherings = IntStream.range(0, TEST_SIZE / 2).boxed()
                .map(i -> genGathering("Gathering" + i, testUser))
                .toList();

        gatheringRepo.saveAll(testGatherings);
    }

    @AfterEach
    void remove() {
        for (JpaRepository<?, ?> repo : new JpaRepository[]{
                userRepo, relatedLinkRepo, portFolioRepo,
                storedProfileImageUrlRepo, archiveRepo, gatheringRepo
        }) {
            repo.deleteAll();
        }
    }

    @Test
    @DisplayName("유저 프로필 정보 가져오기")
    void getUserDetails() {
        log.info("<- getUserDetails");

        String portfolio = testUser.getPortfolios().get(0).getUrl();
        List<String> socials = testUser.getRelatedLinks().stream()
                .map(RelatedLink::getLink).toList();

        // 로그인 안한 사용자가 조회
        UserDetailResponse result = userService.getUserDetails(
                testUser.getId(), Optional.empty()
        );
        checkEquality(result, testUser, false,
                portfolio, socials, representativeColor);

        // 로그인한 다른 유저가 조회
        result = userService.getUserDetails(
                testUser.getId(), Optional.of(User.builder().id(Long.MAX_VALUE).build()));
        checkEquality(result, testUser, false,
                portfolio, socials, representativeColor);

        // 유저가 자기 자신 조회
        result = userService.getUserDetails(
                testUser.getId(), Optional.of(testUser)
        );
        checkEquality(result, testUser, true,
                portfolio, socials, representativeColor);

        // 예외 확인 : Db 에 없는 id 주어졌을 때
        assertThatThrownBy(() -> userService.getUserDetails(
                Long.MAX_VALUE, Optional.empty()
        ))
                .isInstanceOf(UserNotFoundException.class);

        log.info("-> getUserDetails");
    }

    @Test
    @DisplayName("프로필 수정을 위해 유저 정보 가져오기")
    void getUserEditForm() {
        // 사실 `유저 프로필 정보 가져오기` 테스트랑 거의 똑같음
        String portfolio = testUser.getPortfolios().get(0).getUrl();
        List<String> socials = testUser.getRelatedLinks().stream()
                .map(RelatedLink::getLink).toList();

        // 정상 실행
        UserEditFormResponse result = userService.getUserEditForm(
                testUser.getId(), Optional.of(testUser)
        );

        // 결과 검증
        checkEquality(result, testUser, portfolio, socials);

        // 예외 검증
        this.checkException(testUser.getId());
    }

    @Test
    @DisplayName("유저 정보를 수정")
    void editUserInfo() {
        EditUserInfoRequest req = new EditUserInfoRequest(
                "수정됨", "수정됨", "이미지.com",
                "etc", "etc", "수정됨",
                "etc", "포폴 링크.com",
                List.of("111.com", "222.com", "333.com"),
                List.of("111.com", "222.com", "333.com")
        );

        // 정상 실행 검증
        UserResponse result = userService.editUserInfo(req, testUser.getId(),
                Optional.of(testUser));

        assertThat(result.userId()).isEqualTo(testUser.getId());

        List<String> portfolios = portFolioRepo.findAllByUserId(testUser.getId())
                .stream().map(PortFolio::getUrl)
                .toList();

        assertThat(portfolios).hasSize(1);

        List<String> socials = this.getEntityToList(testUser.getId(),
                relatedLinkRepo::findAllByUserId,
                RelatedLink::getLink);

        List<String> s3ImageUrls = this.getEntityToList(testUser.getId(),
                storedProfileImageUrlRepo::findAllByUserId,
                StoredProfileImageUrl::getUrl);

        this.checkEquality(req, testUser, portfolios.get(0), socials, s3ImageUrls);

        assertThat(testUser.getUserRole())
                .isNotEqualTo(UserRole.REAL_NEWBIE)
                .isNotEqualTo(UserRole.JUST_NEWBIE);

        // 예외 검증
        this.checkException(testUser.getId(), req);
    }

    @Test
    @DisplayName("유저가 작성한 아카이브 목록 가져오기")
    void getUserArchives() {

        // 유저의 모든 아카이브 목록 (id 내림차순 = 최신순)
        List<Archive> allArchives = new ArrayList<>(TEST_SIZE);
        allArchives.addAll(blueArchives);
        allArchives.addAll(redArchives);
        allArchives.addAll(noColorArchives);
        allArchives.sort(Comparator.comparing(Archive::getId).reversed());

        int size = 10;
        Long prevArchiveId = null;
        List<SimpleArchiveInfo> prev = null;
        int all = 0;

        // 페이징 잘 되는지 확인
        for (int i = 0; ; i += size) {

            List<Archive> expectedArchives = allArchives.subList(i,
                    Math.min(i + size, allArchives.size()));
            List<String> archiveThumbnails = expectedArchives.stream()
                    .map(Archive::getArchiveImages)
                    .map(images -> images.isEmpty() ? null : images.get(0))
                    .map(image -> image == null ? null : image.getImageUrl())
                    .toList();

            // 결과 검증
            GetUserArchiveResponse result = userService.getUserArchives(
                    testUser.getId(), size, prevArchiveId
            );
            List<SimpleArchiveInfo> infoList = result.archives();

            // 예상한 대로 개수 갖고 있는지
            assertThat(infoList.size())
                    .isEqualTo(expectedArchives.size())
                    .isEqualTo(archiveThumbnails.size());

            // 정보 잘 들어 갔는지
            for (int j = 0; j < infoList.size(); j++) {
                this.checkEquality(infoList.get(j),
                        expectedArchives.get(j), archiveThumbnails.get(j));
            }

            // 페이징하면서 겹친거 없는지
            if (prev != null) {
                assertThat(prev).doesNotContainAnyElementsOf(infoList);
            }

            prev = infoList;
            prevArchiveId = result.nextArchiveId();
            all += infoList.size();

            if (!result.hasNext()) {
                break;
            }
        }

        // 전부 가져와 졌는지 확인
        assertThat(all).isEqualTo(allArchives.size());

        // 존재하지 않는 유저꺼 확인하면 비어있어야 됨.
        GetUserArchiveResponse emptyResult = userService.getUserArchives(
                Long.MAX_VALUE, size, null
        );

        assertThat(emptyResult).isNotNull().satisfies(
                er -> assertThat(er.hasNext()).isFalse(),
                er -> assertThat(er.nextArchiveId()).isNull(),
                er -> assertThat(er.archives()).isEmpty()
        );
    }

    @Test
    @DisplayName("유저가 작성한 게더링 목록 가져오기")
    void getUserGatherings() {

        List<Gathering> allGatherings = testGatherings.stream()
                .sorted(Comparator.comparing(Gathering::getId).reversed())
                .toList();

        int size = 10;
        Long prevGatheringId = null;
        List<SimpleGatheringInfo> prev = null;
        int all = 0;

        // 페이징 잘 되는지 확인
        for (int i = 0; ; i += size) {

            List<Gathering> expectedGatherings = allGatherings.subList(i,
                    Math.min(i + size, allGatherings.size()));

            // 결과 검증
            GetUserGatheringResponse result = userService.getUserGatherings(
                    testUser.getId(), size, prevGatheringId
            );
            List<SimpleGatheringInfo> infoList = result.gatherings();

            // 예상한 대로 개수 갖고 있는지
            assertThat(infoList.size())
                    .isEqualTo(expectedGatherings.size());

            // 정보 잘 들어 갔는지
            for (int j = 0; j < infoList.size(); j++) {
                this.checkEquality(infoList.get(j), expectedGatherings.get(j));
            }

            // 페이징하면서 겹친거 없는지
            if (prev != null) {
                assertThat(prev).doesNotContainAnyElementsOf(infoList);
            }

            prev = infoList;
            prevGatheringId = result.nextGatheringId();
            all += infoList.size();

            if (!result.hasNext()) {
                break;
            }
        }

        // 전부 가져와 졌는지 확인
        assertThat(all).isEqualTo(allGatherings.size());

        // 존재하지 않는 유저꺼 확인하면 비어있어야 됨.
        GetUserGatheringResponse emptyResult = userService.getUserGatherings(
                Long.MAX_VALUE, size, null
        );

        assertThat(emptyResult).isNotNull().satisfies(
                er -> assertThat(er.hasNext()).isFalse(),
                er -> assertThat(er.nextGatheringId()).isNull(),
                er -> assertThat(er.gatherings()).isEmpty()
        );
    }

    private void checkEquality(UserDetailResponse response, User origin,
            boolean checkRoleEither, String portfolioLink,
            List<String> socials, ArchiveType representativeColor
    ) {
        // 기본 유저 정보 확인
        assertThat(response).isNotNull().satisfies(
                r -> assertThat(r.name()).isEqualTo(origin.getName()),
                r -> assertThat(r.email()).isEqualTo(origin.getEmail()),
                r -> assertThat(r.briefIntro()).isEqualTo(origin.getBriefIntro()),
                r -> assertThat(r.imageUrl()).isEqualTo(origin.getImageUrl()),
                r -> assertThat(r.majorJobGroup()).isEqualTo(origin.getMajorJobGroup()),
                r -> assertThat(r.minorJobGroup()).isEqualTo(origin.getMinorJobGroup()),
                r -> assertThat(r.jobTitle()).isEqualTo(origin.getJobTitle()),
                r -> assertThat(r.division()).isEqualTo(origin.getDivision())
        );

        // role 도 맞는지 확인
        if (checkRoleEither) {
            assertThat(response.role()).isNotNull()
                    .isEqualTo(origin.getUserRole());
        } else {
            assertThat(response.role()).isNull();
        }

        // 포폴 링크 확인
        assertThat(response.portfolioLink()).isEqualTo(portfolioLink);

        // 소셜 링크 확인
        List<String> socialLinks = response.socials().stream().sorted().toList();
        assertThat(socials.stream().sorted().toList())
                .isEqualTo(socialLinks);

        // 유저 대표 색상 확인
        assertThat(response.color()).isEqualTo(representativeColor);

        log.info("Response are equal to given.");
    }

    private void checkEquality(UserEditFormResponse response, User origin,
            String portfolioLink, List<String> socials) {
        // 기본 유저 정보 확인
        assertThat(response).isNotNull().satisfies(
                r -> assertThat(r.name()).isEqualTo(origin.getName()),
                r -> assertThat(r.email()).isEqualTo(origin.getEmail()),
                r -> assertThat(r.briefIntro()).isEqualTo(origin.getBriefIntro()),
                r -> assertThat(r.imageUrl()).isEqualTo(origin.getImageUrl()),
                r -> assertThat(r.majorJobGroup()).isEqualTo(origin.getMajorJobGroup()),
                r -> assertThat(r.minorJobGroup()).isEqualTo(origin.getMinorJobGroup()),
                r -> assertThat(r.jobTitle()).isEqualTo(origin.getJobTitle()),
                r -> assertThat(r.division()).isEqualTo(origin.getDivision())
        );

        // 포폴 링크 확인
        assertThat(response.portfolioLink()).isEqualTo(portfolioLink);

        // 소셜 링크 확인
        List<String> socialLinks = response.socials().stream().sorted().toList();
        assertThat(socials.stream().sorted().toList())
                .isEqualTo(socialLinks);

        log.info("Response are equal to given.");
    }

    private void checkEquality(EditUserInfoRequest request, User target,
            String portfolio, List<String> url, List<String> s3Urls) {
        assertThat(request).isNotNull().satisfies(
                r -> assertThat(r.name()).isEqualTo(target.getName()),
                r -> assertThat(r.briefIntro()).isEqualTo(target.getBriefIntro()),
                r -> assertThat(r.imageUrl()).isEqualTo(target.getImageUrl()),
                r -> assertThat(r.jobTitle()).isEqualTo(target.getJobTitle())
        );

        Division div = Division.of(request.division());
        MajorJobGroup major = MajorJobGroup.of(request.majorJobGroup());
        MinorJobGroup minor = MinorJobGroup.of(request.minorJobGroup());

        assertThat(div).isNotNull().isEqualTo(target.getDivision());
        assertThat(major).isNotNull().isEqualTo(target.getMajorJobGroup());
        assertThat(minor).isNotNull().isEqualTo(target.getMinorJobGroup());

        assertThat(portfolio).isNotNull().isEqualTo(request.portfolioLink());

        url = url.stream().sorted().toList();
        s3Urls = s3Urls.stream().sorted().toList();

        assertThat(url).isEqualTo(request.url().stream().sorted().toList());
        assertThat(s3Urls).isEqualTo(request.s3StoredImageUrls().stream().sorted().toList());

        log.info("User info were successfully edited.");
    }


    private void checkEquality(SimpleArchiveInfo result, Archive origin, String thumbnailUrl) {
        assertThat(result).isNotNull().satisfies(
                r -> assertThat(r.archiveId()).isNotNull().isEqualTo(origin.getId()),
                r -> assertThat(r.title()).isNotNull().isEqualTo(origin.getTitle()),
                r -> assertThat(r.color()).isNotNull().isEqualTo(origin.getType())
        );

        assertThat(result.thumbnailImageUrl()).isEqualTo(thumbnailUrl);
    }

    // TODO : 게더링 이미지 추가되면 썸네일 주소 검증도 필요함
    private void checkEquality(SimpleGatheringInfo result, Gathering origin) {
        assertThat(result).isNotNull().satisfies(
                r -> assertThat(r.gatheringId()).isEqualTo(origin.getId()),
                r -> assertThat(r.title()).isEqualTo(origin.getTitle())
        );
    }

    private void checkException(Long userId) {
        // 예외 확인 : db 에 없는 id 제공
        assertThatThrownBy(() -> userService.getUserEditForm(Long.MAX_VALUE, Optional.empty()))
                .isInstanceOf(UserNotFoundException.class);

        // 예외 확인 : 로그인 안한 사용자
        assertThatThrownBy(() -> userService.getUserEditForm(userId, Optional.empty()))
                .isInstanceOf(NotOwnUserException.class);

        // 예외 확인 : 다른 사용자가 접근
        assertThatThrownBy(() -> userService.getUserEditForm(
                userId, Optional.of(User.builder().id(Long.MAX_VALUE).build())
        )).isInstanceOf(NotOwnUserException.class);
    }

    private void checkException(Long userId, EditUserInfoRequest req) {
        // 예외 확인 : db 에 없는 id 제공
        assertThatThrownBy(() -> userService.editUserInfo(
                req, Long.MAX_VALUE, Optional.empty()
        ))
                .isInstanceOf(UserNotFoundException.class);

        // 예외 확인 : 로그인 안한 사용자
        assertThatThrownBy(() -> userService.editUserInfo(
                req, userId, Optional.empty()
        ))
                .isInstanceOf(NotOwnUserException.class);

        // 예외 확인 : 다른 사용자가 접근
        assertThatThrownBy(() -> userService.editUserInfo(
                req, userId, Optional.of(User.builder().id(Long.MAX_VALUE).build())

        )).isInstanceOf(NotOwnUserException.class);
    }

    private <E> List<String> getEntityToList(Long userId,
            Function<Long, List<E>> searchFunc,
            Function<E, String> convertFunc) {
        return searchFunc.apply(userId).stream().map(convertFunc).toList();
    }
}