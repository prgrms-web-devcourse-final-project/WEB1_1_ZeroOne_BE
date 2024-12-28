package com.palettee.user.service;

import com.palettee.archive.domain.*;
import com.palettee.archive.repository.*;
import com.palettee.gathering.repository.*;
import com.palettee.global.security.jwt.services.*;
import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.controller.dto.response.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepo;
    private final RelatedLinkRepository relatedLinkRepo;
    private final PortFolioRepository portFolioRepo;
    private final StoredProfileImageUrlRepository storedProfileImageUrlRepo;
    private final ArchiveRepository archiveRepo;
    private final GatheringRepository gatheringRepo;
    private final GatheringTagRepository gatheringTagRepo;
    private final RefreshTokenRedisService refreshTokenRedisService;

    /**
     * 자신의 정보를 조회
     *
     * @param loggedInUser 로그인한 유저
     */
    public SimpleUserResponse getMyInfo(Optional<User> loggedInUser) {
        return SimpleUserResponse.of(
                loggedInUser.orElseThrow(() -> UserNotFoundException.EXCEPTION)
        );
    }

    /**
     * 유저 로그아웃
     *
     * @param loggedInUser 로그인한 유저
     */
    @Transactional
    public UserResponse logout(Optional<User> loggedInUser) {

        User userOnLogout = loggedInUser.orElseThrow(() -> UserNotFoundException.EXCEPTION);
        refreshTokenRedisService.deleteRefreshToken(userOnLogout);

        log.info("User {}'s refresh token in redis were removed.", userOnLogout.getId());

        return UserResponse.of(userOnLogout);
    }

    /**
     * 유저 프로필 정보 반환하는 메서드
     * <p>
     * 만약 자신의 프로필을 조회하면 {@link UserDetailResponse#role} 은 {@code non-null} 값으로 응답
     *
     * @param userId       프로필 조회할 유저 id
     * @param loggedInUser 지금 로그인한 유저
     * @throws UserNotFoundException id 에 해당하는 유저가 없을 때
     */
    public UserDetailResponse getUserDetails(Long userId, Optional<User> loggedInUser)
            throws UserNotFoundException {
        // 조회하려는 유저 info
        User userOnTarget = this.getUserById(userId);

        // 포폴 링크
        String portfolioLink = this.getPortfolioLink(userOnTarget);

        // linkedin, 블로그 등
        List<String> socials = this.getSocials(userOnTarget);

        // 유저 대표 색상 가져오기
        Map<ArchiveType, Long> colorMap = userOnTarget.getArchives().stream()
                .filter(archive -> !archive.getType().equals(ArchiveType.NO_COLOR))
                .collect(Collectors.groupingBy(Archive::getType, Collectors.counting()));
        Optional<ArchiveType> representativeColor = colorMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);

        // 지금 조회하는 프로필이 자기껀지 확인
        boolean isMine = loggedInUser.isPresent() &&
                loggedInUser.get().getId().equals(userOnTarget.getId());

        return UserDetailResponse.of(userOnTarget, portfolioLink, socials,
                representativeColor.orElse(ArchiveType.NO_COLOR), isMine);
    }

    /**
     * 유저가 프로필 정보 수정을 위해 저장된 정보 가져오는 메서드
     *
     * @param userId       정보 조회할 유저 id
     * @param loggedInUser 현재 로그인된 유저
     * @throws UserNotFoundException id 에 해당하는 유저가 없을 때
     * @throws NotOwnUserException   로그인 안 되어 있거나 다른 유저의 정보를 조회하려 할 때
     */
    public UserEditFormResponse getUserEditForm(
            Long userId, Optional<User> loggedInUser)
            throws UserNotFoundException, NotOwnUserException {

        User userOnTarget = this.getUserById(userId);

        // 자기 자신만 자기 정보 변경할 수 있음.
        this.checkUsersAreSame(userOnTarget, loggedInUser);

        String portfolioLink = this.getPortfolioLink(userOnTarget);
        List<String> socials = this.getSocials(userOnTarget);

        log.info("Loaded user {}'s info successfully", userOnTarget.getId());

        return UserEditFormResponse.of(userOnTarget, portfolioLink, socials);
    }

    /**
     * 유저의 정보를 수정하는 메서드
     *
     * @param editUserInfoRequest 수정 요청
     * @param userId              수정할 유저 id
     * @param loggedInUser        현재 로그인된 유저
     * @throws UserNotFoundException id 에 해당하는 유저가 없을 때
     * @throws NotOwnUserException   로그인 안 되어 있거나 다른 유저의 정보를 수정하려 할 때
     */
    @Transactional
    public UserResponse editUserInfo(EditUserInfoRequest editUserInfoRequest,
            Long userId, Optional<User> loggedInUser)
            throws UserNotFoundException, NotOwnUserException {

        User userOnTarget = this.getUserById(userId);

        // 자기 자신만 정보 변경할 수 있음.
        this.checkUsersAreSame(userOnTarget, loggedInUser);

        String portfolioLink = editUserInfoRequest.portfolioLink();

        // 포폴 있으면 OLD_NEWBIE 로, 없으면 JUST_NEWBIE 까지 상승
        UserRole targetRole = portfolioLink != null && !portfolioLink.isEmpty() ?
                UserRole.OLD_NEWBIE : UserRole.JUST_NEWBIE;
        userOnTarget.changeUserRole(targetRole);

        log.debug("User {}'s role has been updated up to {}",
                userOnTarget.getId(), targetRole);

        userOnTarget.update(editUserInfoRequest);
        log.debug("Edited basic user {}'s info", userOnTarget.getId());

        userOnTarget = this.getUserByIdFetchWithRelatedLinks(userId);

        // 일단 기본 정보만 등록
        relatedLinkRepo.deleteAllByUserId(userOnTarget.getId());
        log.debug("Deleted user {}'s all social links", userOnTarget.getId());

        // socials (linkedin, 블로그 등) 정보 등록
        List<String> links = editUserInfoRequest.socials();
        // 등록 되었다면 log 찍기
        if (this.registerUrlsOn(userOnTarget, links,
                relatedLinkRepo::save, RelatedLink::new)) {
            log.debug("Edited user {}'s social links", userOnTarget.getId());
        }

        userOnTarget = this.getUserByIdFetchWithPortfolio(userId);

        // 이전 포폴 정보 삭제
        portFolioRepo.deleteAllByUserId(userOnTarget.getId());
        log.debug("Deleted user {}'s all portfolio links", userOnTarget.getId());

        portFolioRepo.save(new PortFolio(userOnTarget, portfolioLink,userOnTarget.getMajorJobGroup(), userOnTarget.getMinorJobGroup()));
        log.debug("Edited user {}'s portfolio link", userOnTarget.getId());

        // 사용자가 S3 에 업로드한 자원들 추가
        List<String> s3Resources = editUserInfoRequest.s3StoredImageUrls();

        if (s3Resources != null && !s3Resources.isEmpty()) {
            userOnTarget = this.getUserByIdFetchWithStoredImageUrls(userId);
        }

        // S3 자원들 저장 되었다면 로그 찍기
        if (this.registerUrlsOn(userOnTarget, s3Resources,
                storedProfileImageUrlRepo::save, StoredProfileImageUrl::new)) {
            log.debug("Added S3 uploaded resource URLs on DB");
        }

        log.info("Edited user {}'s info successfully", userOnTarget.getId());

        return UserResponse.of(userOnTarget);
    }

    /**
     * 유저가 작성한 아카이브들의 색상 통계를 보여주는 메서드
     *
     * @param userId 아카이브 색상 통계 조회할 유저 id
     */
    public GetArchiveColorStatisticsResponse getArchiveColorStatistics(
            Long userId
    ) {
        return GetArchiveColorStatisticsResponse.of(
                archiveRepo.findAllByUserId(userId)
        );
    }

    /**
     * 유저가 작성한 아카이브 목록 보여주는 메서드
     *
     * @param userId        아카이브 조회할 유저 id
     * @param size          지금 조회할 아카이브 개수
     * @param prevArchiveId {@code NoOffset} 을 위한 이전 응답에서 제공된 {@code nextArchiveId}
     */
    public GetUserArchiveResponse getUserArchives(
            Long userId, int size, Long prevArchiveId
    ) {
        return archiveRepo.findArchivesOnUserWithNoOffset(
                userId, size, prevArchiveId
        );
    }

    /**
     * 유저가 작성한 게더링 목록 보여주는 메서드
     *
     * @param userId          게더링 조회할 유저 id
     * @param size            지금 조회할 게더링 개수
     * @param prevGatheringId {@code NoOffset} 을 위한 이전 응답에서 제공된 {@code nextGatheringId}
     */
    public GetUserGatheringResponse getUserGatherings(
            Long userId, int size, Long prevGatheringId
    ) {
        GatheringPagingDTO pagingDTO = gatheringRepo.findGatheringsOnUserWithNoOffset(
                userId, size, prevGatheringId
        );

        return GetUserGatheringResponse.of(pagingDTO, gatheringTagRepo);
    }


    private User getUserById(Long userId) throws UserNotFoundException {
        return userRepo.findById(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    private User getUserByIdFetchWithRelatedLinks(Long userId) throws UserNotFoundException {
        return userRepo.findByIdFetchWithRelatedLinks(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    private User getUserByIdFetchWithPortfolio(Long userId) throws UserNotFoundException {
        return userRepo.findByIdFetchWithPortfolios(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    private User getUserByIdFetchWithStoredImageUrls(Long userId) throws UserNotFoundException {
        return userRepo.findByIdFetchWithStoredProfileUrls(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }


    // 유저 포폴 링크 가져오기
    private String getPortfolioLink(User user) {
        List<PortFolio> portFolios = portFolioRepo.findAllByUserId(user.getId());
        // 처음에 한 유저당 포폴 여러개 저장 가능으로 설계했어서 `get(0)`
        return portFolios.isEmpty() ? null : portFolios.get(0).getUrl();
    }

    // 유저 소셜 링크들 가져오기
    private List<String> getSocials(User user) {
        return user.getRelatedLinks().stream()
                .map(RelatedLink::getLink)
                .toList();
    }

    /**
     * 주어진 {@code owner} 와 {@code loggedInUser} 가 같은 사람인지 확인하는 메서드
     *
     * @throws NotOwnUserException 로그인 안되있거나 다른 유저일 때
     */
    private void checkUsersAreSame(User owner, Optional<User> loggedInUser)
            throws NotOwnUserException {
        if (loggedInUser.isEmpty() || !loggedInUser.get().getId().equals(owner.getId())) {
            log.error("Anonymous user attempted access to user {}'s resource.", owner.getId());
            throw NotOwnUserException.EXCEPTION;
        }

        log.info("User {} access to his resource.", owner.getId());
    }

    private <E> boolean registerUrlsOn(User user, List<String> urls,
            Function<E, E> repoSaveFunc, BiFunction<String, User, E> entityMaker) {

        if (urls == null || urls.isEmpty()) {
            return false;
        }

        for (String url : urls) {
            E entity = entityMaker.apply(url, user);    // new Entity(url, user)
            repoSaveFunc.apply(entity);                 // repo.save(entity)
        }

        return true;
    }
}
