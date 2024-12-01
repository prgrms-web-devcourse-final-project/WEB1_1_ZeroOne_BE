package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class InvalidReportTypeException extends PaletteException {

    public static PaletteException EXCEPTION = new InvalidReportTypeException();

    private InvalidReportTypeException() {
        super(ErrorCode.INVALID_REPORT_TYPE);
    }
}
