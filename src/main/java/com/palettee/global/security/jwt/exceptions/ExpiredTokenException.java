package com.palettee.global.security.jwt.exceptions;

import com.palettee.global.exception.*;

public class ExpiredTokenException extends PaletteException {

    public static final PaletteException EXCEPTION = new ExpiredTokenException();

    private ExpiredTokenException() {
        super(ErrorCode.EXPIRED_TOKEN);
    }
}
