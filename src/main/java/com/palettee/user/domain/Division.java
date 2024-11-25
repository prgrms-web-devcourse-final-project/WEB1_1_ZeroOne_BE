package com.palettee.user.domain;

import com.palettee.user.exception.*;
import java.util.*;
import lombok.*;

/**
 * 유저 소속 enum
 */
@AllArgsConstructor
public enum Division {
    STUDENT, WORKER, ETC;

    public static Division of(String division) {
        return Arrays.stream(Division.values())
                .filter(d -> d.toString().equals(division.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> InvalidDivisionException.EXCEPTION);
    }
}
