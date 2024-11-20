package com.palettee.user.domain;

import lombok.*;

@RequiredArgsConstructor
public enum MinorJobGroup {
    FRONTEND("프론트엔드 개발자"),
    BACKEND("서버/백엔드 개발자"),
    FULL_STACK("풀스택 개발자"),
    SOFTWARE("소프트웨어 엔지니어"),
    ANDROID("Android 개발자"),
    IOS("IOS 개발자"),
    CROSS_PLATFORM("크로스플랫폼 앱 개발자"),
    DEV_OPS("DevOps 엔지니어"),
    SYSTEM("시스템/네트워크 관리"),
    MACHINE_LEARNING("머신러닝 엔지니어"),
    QA("QA 엔지니어"),
    DATA_ENGINEER("데이터 엔지니어"),
    DATA_SCIENCE("데이터 사이언티스트"),
    SECURITY("보안 엔지니어"),
    HW_EMBEDDED("HW/임베디드 개발자"),
    BLOCK_CHAIN("블록체인 엔지니어"),
    DBA("DBA"),
    GAME("게임 개발자"),
    ETC_DEV("기타"),

    /* -------- 개발 -------- */

    PM_PO("PM ∘ PO"),
    SERVICE("서비스 기획자"),
    STRATEGY("전략 기획자"),
    BUSINESS("사업개발 기획자"),
    ANALYSIS("비즈니스 분석가"),
    MD("상품 기획자/MD"),
    ETC_PROM("다른 기획 분야"),

    /* -------- 기획 -------- */

    DESIGN("일반 디자인"),
    INSPIRATION("인스퍼레이션"),
    UI_UX("UI/UX"),
    WITH_CODING("코딩하는 디자이너"),
    BRAND_DESIGN("브랜드 디자이너"),
    TYPO("타이포그래피"),
    RESOURCE("디자인 리소스"),


    /* -------- 디자인 -------- */

    MARKETING("일반 마케팅"),
    BRAND("브랜드 마케팅"),
    GROWTH("그로스 마케팅"),
    CONTENTS("콘텐츠 마케팅"),
    INSIGHT("마케팅 인사이트"),


    /* -------- 마케팅 -------- */

    ETC_SERVICE("서비스 업"),
    DISTRIBUTION("판매, 유통"),
    CONSTRUCT("건설업"),
    DELIVERY("운전, 운송, 배송"),
    EDUCATION("교육업"),
    FINANCE("은행, 금융업"),
    WELFARE("공공, 복지"),
    ETC("그 외");

    /* -------- 기타 -------- */


    private final String minorJobGroup;

    public String getMinorJobGroup() {
        return minorJobGroup;
    }
}
