package com.palettee.user.domain;

import com.palettee.user.exception.*;
import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum MinorJobGroup {
    FRONTEND("프론트엔드 개발자", 0),
    BACKEND("서버/백엔드 개발자", 0),
    FULL_STACK("풀스택 개발자", 0),
    SOFTWARE("소프트웨어 엔지니어", 0),
    ANDROID("Android 개발자", 0),
    IOS("IOS 개발자", 0),
    CROSS_PLATFORM("크로스플랫폼 앱 개발자", 0),
    DEV_OPS("DevOps 엔지니어", 0),
    SYSTEM("시스템/네트워크 관리", 0),
    MACHINE_LEARNING("머신러닝 엔지니어", 0),
    QA("QA 엔지니어", 0),
    DATA_ENGINEER("데이터 엔지니어", 0),
    DATA_SCIENCE("데이터 사이언티스트", 0),
    SECURITY("보안 엔지니어", 0),
    HW_EMBEDDED("HW/임베디드 개발자", 0),
    BLOCK_CHAIN("블록체인 엔지니어", 0),
    DBA("DBA", 0),
    GAME("게임 개발자", 0),
    ETC_DEV("기타", 0),

    /* -------- 개발 -------- */

    PM_PO("PM ∘ PO", 1),
    SERVICE("서비스 기획자", 1),
    STRATEGY("전략 기획자", 1),
    BUSINESS("사업개발 기획자", 1),
    ANALYSIS("비즈니스 분석가", 1),
    MD("상품 기획자/MD", 1),
    ETC_PROM("다른 기획 분야", 1),

    /* -------- 기획 -------- */

    DESIGN("일반 디자인", 2),
    INSPIRATION("인스퍼레이션", 2),
    UI_UX("UI/UX", 2),
    WITH_CODING("코딩하는 디자이너", 2),
    BRAND_DESIGN("브랜드 디자이너", 2),
    TYPO("타이포그래피", 2),
    RESOURCE("디자인 리소스", 2),


    /* -------- 디자인 -------- */

    MARKETING("일반 마케팅", 3),
    BRAND("브랜드 마케팅", 3),
    GROWTH("그로스 마케팅", 3),
    CONTENTS("콘텐츠 마케팅", 3),
    INSIGHT("마케팅 인사이트", 3),


    /* -------- 마케팅 -------- */

    ETC_SERVICE("서비스 업", 4),
    DISTRIBUTION("판매, 유통", 4),
    CONSTRUCT("건설업", 4),
    DELIVERY("운전, 운송, 배송", 4),
    EDUCATION("교육업", 4),
    FINANCE("은행, 금융업", 4),
    WELFARE("공공, 복지", 4),
    ETC("그 외", 4);

    /* -------- 기타 -------- */


    private final String minorJobGroup;
    private final int jobIdentity;

    public static MinorJobGroup findMinorJobGroup(String input) {
        return Arrays.stream(MinorJobGroup.values())
                .filter(it -> it.minorJobGroup.equals(input))
                .findFirst()
                .orElse(null);
    }

    public static MinorJobGroup of(String group) {
        MinorJobGroup minorGroup = findMinorJobGroup(group.toUpperCase());

        if (minorGroup == null) {
            throw InvalidJobGroupException.EXCEPTION;
        }

        return minorGroup;
    }
}
