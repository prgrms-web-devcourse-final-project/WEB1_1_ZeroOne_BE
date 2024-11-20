package com.palettee.user.domain;

import lombok.*;

@RequiredArgsConstructor
public enum MajorJobGroup {
    DEVELOPER("개발"),
    PROMOTER("기획"),
    DESIGN("디자인"),
    MARKETING("마케팅"),
    ETC("기타");

    private final String majorGroup;

    public String getMajorGroup() {
        return majorGroup;
    }
}
