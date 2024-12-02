package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class InvalidDivisionException extends PaletteException {

    public static PaletteException EXCEPTION = new InvalidDivisionException();

    private InvalidDivisionException() {
        super(ErrorCode.INVALID_DIVISION);
    }
}
