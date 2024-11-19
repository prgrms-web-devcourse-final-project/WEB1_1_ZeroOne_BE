package com.palettee.user.domain;

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
public enum UserRole {
    REAL_NEWBIE, JUST_NEWBIE,
    OLD_NEWBIE, USER, ADMIN
}
