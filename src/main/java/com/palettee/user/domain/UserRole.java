package com.palettee.user.domain;

import lombok.*;

/**
 * 유저 권한을 나타내는 {@code enum}
 *
 * <pre>
 * {@code
 * - REAL_NEWBIE : 회원가입 후 기본 정보조차 등록 안된 유저 (이름, 자기소개조차 안 적은 유저)
 * - JUST_NEWBIE : 회원가입 후 포트폴리오 등록 안된 유저
 * - OLD_NEWBIE : 회원가입 후 프로젝트 등록 안된 유저
 * - USER : 기본 정보, 포폴, 프젝 모두 등록된 유저
 * - ADMIN : 관리자 유저
 * }
 * </pre>
 */
@RequiredArgsConstructor
public enum UserRole {
    REAL_NEWBIE(0), JUST_NEWBIE(1),
    OLD_NEWBIE(2), USER(3), ADMIN(4);

    private final int value;

    /**
     * 현재 권한 {@code (curr)} 과 {@code target} 을 비교해, {@code target} 보다 높거나 같은 권한을 뱉는 메서드
     * <p>
     * 만약 {@code curr} 이 {@code target} 보다 높거나 같으면 {@code curr} 을 반환
     * <p>
     * 그렇지 않다면 {@code target} 권한을 반환
     * <p>
     * {@code USER -> ADMIN} 상승은 이뤄지지 않음.
     *
     * @param curr 현재 권한
     * @return {@code target} 보다 높거나 같은 권한
     */
    public static UserRole upgrade(UserRole curr, UserRole target) {

        if (target.value <= curr.value) {
            return curr;
        } else if (target == ADMIN) {
            return USER;
        } else {
            return target;
        }
    }
}
