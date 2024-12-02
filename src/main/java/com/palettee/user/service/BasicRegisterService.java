package com.palettee.user.service;

import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.controller.dto.response.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

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

        // url 제외 정보 등록
        user = this.getUser(user.getEmail());
        user.update(registerBasicInfoRequest);
        user.changeUserRole(UserRole.JUST_NEWBIE);

        log.debug("Registered user {}'s basic info", user.getId());

        user = this.getUserByIdFetchWithRelatedLinks(user.getId());

        // 이전 저장되 있던 url 제거
        relatedLinkRepo.deleteAllByUserId(user.getId());

        log.debug("Deleted user {}'s all social links", user.getId());

        // url (linkedin, 블로그 등) 정보 등록
        List<String> links = registerBasicInfoRequest.url();
        // url 등록 되었으면 로그 찍기
        if (this.registerUrlsOn(user, links, relatedLinkRepo::save, RelatedLink::new)) {
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
