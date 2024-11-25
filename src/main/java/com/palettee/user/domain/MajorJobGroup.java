package com.palettee.user.domain;

import com.palettee.user.exception.*;
import java.util.*;
import lombok.*;

@Getter
@RequiredArgsConstructor
public enum MajorJobGroup {
    DEVELOPER("개발", 0),
    PROMOTER("기획", 1),
    DESIGN("디자인", 2),
    MARKETING("마케팅", 3),
    ETC("기타", 4);

    private final String majorGroup;
    private final int jobIdentity;

    public static MajorJobGroup findMajorGroup(String input) {
        return Arrays.stream(MajorJobGroup.values())
                .filter(it -> it.majorGroup.equals(input))
                .findFirst()
                .orElse(null);
    }

    public static MajorJobGroup of(String group) throws InvalidJobGroupException {
        String upper = group.toUpperCase();

        MajorJobGroup majorGroup = Arrays.stream(MajorJobGroup.values())
                .filter(job -> job.toString().equals(upper))
                .findFirst()
                .orElse(null);

        if (majorGroup == null) {
            throw InvalidJobGroupException.EXCEPTION;
        }

        return majorGroup;
    }

    public boolean matches(MinorJobGroup minorJobGroup) {
        return this.jobIdentity == minorJobGroup.getJobIdentity();
    }
}
