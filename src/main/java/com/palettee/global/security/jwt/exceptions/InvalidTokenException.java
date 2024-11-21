package com.palettee.global.security.jwt.exceptions;

import com.palettee.global.exception.*;

public class InvalidTokenException extends PaletteException {

    public static final PaletteException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
