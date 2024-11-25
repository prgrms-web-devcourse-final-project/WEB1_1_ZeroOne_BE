package com.palettee.user.service;

import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.*;
import com.palettee.user.controller.dto.response.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
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
        user = user.update(registerBasicInfoRequest);

        // 이전 저장되 있던 url 제거
        relatedLinkRepo.deleteAllByUserId(user.getId());

        // url (linkedin, 블로그 등) 정보 등록
        for (String link : registerBasicInfoRequest.url()) {
            relatedLinkRepo.save(new RelatedLink(link, user));
        }

        // 기본 정보 등록했으니까 권한 상승
        user.changeUserRole(UserRole.JUST_NEWBIE);

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

        // 이전 포폴 정보 삭제
        portFolioRepo.deleteAllByUserId(user.getId());

        // 포폴 정보 등록
        String url = registerPortfolioRequest.portfolioUrl();
        portFolioRepo.save(new PortFolio(user, url));

        // 권한 상승
        user.changeUserRole(UserRole.OLD_NEWBIE);

        log.info("Registered user portfolio info on id: {}",
                user.getId());

        return UserResponse.of(user);
    }

    private User getUser(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
