package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class ReportNotFoundException extends PaletteException {

    public static PaletteException EXCEPTION = new ReportNotFoundException();

    private ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }
}
