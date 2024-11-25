package com.palettee.user.service;

import com.palettee.global.security.validation.*;
import com.palettee.portfolio.domain.*;
import com.palettee.portfolio.repository.*;
import com.palettee.user.controller.dto.request.*;
import com.palettee.user.controller.dto.response.*;
import com.palettee.user.domain.*;
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

    private final RelatedLinkRepository relatedLinkRepo;
    private final PortFolioRepository portFolioRepo;

    /**
     * 유저 기본 정보 등록시 기초 정보 보여주기
     */
    public BasicInfoResponse showBasicInfo() {
        return BasicInfoResponse.of(getUserFromContext());
    }

    /**
     * 유저 기본 정보 등록하기
     */
    @Transactional
    public UserResponse registerBasicInfo(RegisterBasicInfoRequest registerBasicInfoRequest) {

        // url 제외 정보 등록
        User user = getUserFromContext()
                .update(registerBasicInfoRequest);

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
    public UserResponse registerPortfolio(RegisterPortfolioRequest registerPortfolioRequest) {

        User user = getUserFromContext();
        String url = registerPortfolioRequest.portfolioUrl();

        // 포폴 정보 등록
        portFolioRepo.save(new PortFolio(user, url));

        // 권한 상승
        user.changeUserRole(UserRole.OLD_NEWBIE);

        log.info("Registered user portfolio info on id: {}",
                user.getId());

        return UserResponse.of(user);
    }

    private User getUserFromContext() {
        return UserUtils.getContextUser();
    }
}
