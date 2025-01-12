package com.palettee.user.controller;

import com.palettee.global.security.validation.*;
import com.palettee.portfolio.repository.PortFolioRedisRepository;
import com.palettee.user.controller.dto.request.users.*;
import com.palettee.user.controller.dto.response.users.*;
import com.palettee.user.domain.*;
import com.palettee.user.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final PortFolioRedisRepository redisRepository;

    private static final String REFRESH_TOKEN_COOKIE_KEY = "refresh_token";

    /**
     * 자기 자신의 정보를 조회
     */
    @GetMapping("/my-info")
    public SimpleUserResponse getMyInfo() {
        return userService.getMyInfo(getUserFromContext());
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public UserResponse logout(HttpServletResponse resp) {
        this.removeRefreshTokenOnCookie(resp);
        return userService.logout(getUserFromContext());
    }

    /**
     * 특정 유저의 프로필을 조회
     *
     * @param id 조회하고자 하는 사용자의 id
     */
    @GetMapping("/{userId}/profile")
    public UserDetailResponse getUserDetail(@PathVariable("userId") Long id) {
        return userService.getUserDetails(id, getUserFromContext());
    }

    /**
     * 자신의 프로필을 수정하기 위해 이전 저장된 정보를 조회
     *
     * @param id 조회하고자 하는 사용자의 id (자기 자신)
     */
    @GetMapping("/{userId}/edit")
    public UserEditFormResponse getUserEditForm(@PathVariable("userId") Long id) {
        return userService.getUserEditForm(id, getUserFromContext());
    }

    /**
     * 자신의 프로필을 수정
     *
     * @param id 수정하고자 하는 사용자의 id (자기 자신)
     */
    @PutMapping("/{userId}/edit")
    public UserSavePortFolioResponse editUserInfo(
            @PathVariable("userId") Long id,
            @Valid @RequestBody
            EditUserInfoRequest editUserInfoRequest
    ) {
        UserSavePortFolioResponse userSavePortFolioResponse = userService.editUserInfo(editUserInfoRequest, id, getUserFromContext());

        redisRepository.updatePortFolio(userSavePortFolioResponse.portFolioId());

        return userSavePortFolioResponse;
    }

    /**
     * 유저가 작성한 아카이브들의 색상 통계를 조회
     *
     * @param id 아카이브 색상 통계 조회할 유저 id
     */
    @GetMapping("/{userId}/archive-colors")
    public GetArchiveColorStatisticsResponse getArchiveColorStatistics(
            @PathVariable("userId") Long id
    ) {
        return userService.getArchiveColorStatistics(id);
    }

    /**
     * 특정 유저의 {@code Archive} 를 조회
     *
     * @param id 조회하고자 하는 사용자의 id
     */
//    @GetMapping("/{userId}/archives")
//    public GetUserArchiveResponse getUserArchives(
//            @PathVariable("userId") Long id,
//            @Min(1) @RequestParam("size") int size,
//            @RequestParam(value = "nextArchiveId", required = false) Long nextArchiveId
//    ) {
//        return userService.getUserArchives(
//                id, size, nextArchiveId
//        );
//    }

    /**
     * 특정 유저의 {@code Gathering} 을 조회
     *
     * @param id 조회하고자 하는 사용자의 id
     */
    @GetMapping("/{userId}/gatherings")
    public GetUserGatheringResponse getUserGatherings(
            @PathVariable("userId") Long id,
            @Min(1) @RequestParam("size") int size,
            @RequestParam(value = "nextGatheringId", required = false) Long nextGatheringId
    ) {
        return userService.getUserGatherings(
                id, size, nextGatheringId
        );
    }

    private Optional<User> getUserFromContext() {
        User user = null;
        try {
            user = UserUtils.getContextUser();
        } catch (Exception e) {
            log.info("Current user is not logged in");
        }

        return Optional.ofNullable(user);
    }

    private void removeRefreshTokenOnCookie(HttpServletResponse resp) {

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, null);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        resp.addCookie(cookie);

        log.info("Refresh token in cookie were removed.");
    }
}
