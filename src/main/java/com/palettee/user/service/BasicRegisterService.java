package com.palettee.user.service;

import com.palettee.portfolio.domain.PortFolio;
import com.palettee.portfolio.repository.PortFolioRepository;
import com.palettee.user.controller.dto.request.users.RegisterBasicInfoRequest;
import com.palettee.user.controller.dto.request.users.RegisterPortfolioRequest;
import com.palettee.user.controller.dto.response.users.BasicInfoResponse;
import com.palettee.user.controller.dto.response.users.UserResponse;
import com.palettee.user.domain.RelatedLink;
import com.palettee.user.domain.StoredProfileImageUrl;
import com.palettee.user.domain.User;
import com.palettee.user.domain.UserRole;
import com.palettee.user.exception.InvalidDivisionException;
import com.palettee.user.exception.InvalidJobGroupException;
import com.palettee.user.exception.JobGroupMismatchException;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.RelatedLinkRepository;
import com.palettee.user.repository.StoredProfileImageUrlRepository;
import com.palettee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 유저 기본 정보 등록과 관련된 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicRegisterService {

    private final UserRepository userRepo;
    private final RelatedLinkRepository relatedLinkRepo;
    private final PortFolioRepository portFolioRepo;
    private final StoredProfileImageUrlRepository storedProfileImageUrlRepo;

    /**
     * 유저 기본 정보 등록시 기초 정보 보여주기
     */
    public BasicInfoResponse showBasicInfo(User user) {
        return BasicInfoResponse.of(user);
    }

    /**
     * 유저 기본 정보 등록하기
     *
     * @param user                     등록할 유저
     * @param registerBasicInfoRequest 등록 요청
     * @throws InvalidDivisionException  요청의 소속 잘못 주어진 경우
     * @throws InvalidJobGroupException  요청의 직군 {@code 대분류}, {@code 소분류} 이 잘못 주어진 경우
     * @throws JobGroupMismatchException 요청의 직군 {@code 대분류}, {@code 소분류} 가 잘못 이어진 경우
     */
    @Transactional
    public UserResponse registerBasicInfo(User user,
            RegisterBasicInfoRequest registerBasicInfoRequest)
            throws InvalidDivisionException, InvalidJobGroupException, JobGroupMismatchException {

        // 일단 기본 정보만 등록
        user = this.getUser(user.getEmail());
        user.update(registerBasicInfoRequest);
        user.changeUserRole(UserRole.JUST_NEWBIE);

        log.debug("Registered user {}'s basic info", user.getId());

        user = this.getUserByIdFetchWithRelatedLinks(user.getId());

        // 이전 저장되 있던 socials 제거
        relatedLinkRepo.deleteAllByUserId(user.getId());

        log.debug("Deleted user {}'s all social links", user.getId());

        // socials (linkedin, 블로그 등) 정보 등록
        List<String> socialLinks = registerBasicInfoRequest.socials();
        // socials 등록 되었으면 로그 찍기
        if (this.registerUrlsOn(user, socialLinks, relatedLinkRepo::save, RelatedLink::new)) {
            log.debug("Registered user {}'s social links", user.getId());
        }

        user = this.getUserByIdFetchWithStoredImageUrls(user.getId());

        // 사용자가 S3 에 업로드한 자원들 추가
        List<String> s3Resources = registerBasicInfoRequest.s3StoredImageUrls();
        // S3 자원 url 들 DB 에 저장 되었으면 로그 찍기
        if (this.registerUrlsOn(user, s3Resources,
                storedProfileImageUrlRepo::save, StoredProfileImageUrl::new)) {
            log.debug("Added S3 uploaded resource URLs on DB");
        }

        log.info("Registered basic user info on id: {}",
                user.getId());

        return UserResponse.of(user);
    }

    /**
     * 유저 포폴 정보 (링크) 등록하기
     */
    @Transactional
    @CacheEvict(value = "portFolio_", allEntries = true)
    public UserResponse registerPortfolio(
            User user,
            RegisterPortfolioRequest registerPortfolioRequest
    ) {

        user = this.getUser(user.getEmail());
        user.changeUserRole(UserRole.OLD_NEWBIE);

        user = this.getUserByIdFetchWithPortfolio(user.getId());

        // 이전 포폴 정보 삭제
        portFolioRepo.deleteAllByUserId(user.getId());

        log.debug("Deleted user {}'s all portfolio links", user.getId());

        // 포폴 정보 등록 -> validation 으로 빈 링크는 안들어옴.
        String url = registerPortfolioRequest.portfolioUrl();
        portFolioRepo.save(new PortFolio(user, url));

        log.info("Registered user portfolio info on id: {}",
                user.getId());

        return UserResponse.of(user);
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email)
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

    private <E> boolean registerUrlsOn(User user, List<String> urls,
            Function<E, E> repoSaveFunc, BiFunction<String, User, E> entityMaker) {

        if (urls == null || urls.isEmpty()) {
            return false;
        }

        for (String url : urls) {
            E entity = entityMaker.apply(url, user);  // new Entity(url, user)
            repoSaveFunc.apply(entity);               // repo.save(entity)
        }

        return true;
    }
}
