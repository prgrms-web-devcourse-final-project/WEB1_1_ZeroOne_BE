package com.palettee.user.domain;

import com.palettee.user.exception.*;
import java.util.*;

public enum ReportType {
    BUG, ENHANCEMENT, OTHER;

    public static ReportType of(String reportType) {
        String upper = reportType.toUpperCase();

        return Arrays.stream(ReportType.values())
                .filter(type -> type.toString().equals(upper))
                .findFirst()
                .orElseThrow(() -> InvalidReportTypeException.EXCEPTION);
    }
}
