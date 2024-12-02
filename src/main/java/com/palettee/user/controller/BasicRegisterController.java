package com.palettee.user.controller;

import com.palettee.global.security.validation.*;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.controller.dto.response.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.service.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

/**
 * 유저 기본 정보 등록과 관련된 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class BasicRegisterController {

    private final BasicRegisterService basicRegisterService;

    /**
     * 유저 기본 정보 등록시 기초 정보 보여주기
     */
    @GetMapping("/profile")
    public BasicInfoResponse showBasicInfo() {
        return basicRegisterService.showBasicInfo(getUserFromContext());
    }

    /**
     * 유저 기본 정보 등록하기
     */
    @PostMapping("/profile")
    public UserResponse registerBasicInfo(
            @Valid @RequestBody
            RegisterBasicInfoRequest registerBasicInfoRequest
    ) {
        return basicRegisterService.registerBasicInfo(
                getUserFromContext(), registerBasicInfoRequest
        );
    }

    /**
     * 유저 포폴 정보 등록하기
     */
    @PostMapping("/portfolio")
    public UserResponse registerPortfolio(
            @Valid @RequestBody
            RegisterPortfolioRequest registerPortfolioRequest
    ) {
        return basicRegisterService.registerPortfolio(
                getUserFromContext(), registerPortfolioRequest
        );
    }

    private User getUserFromContext() {
        return UserUtils.getContextUser();
    }
}
