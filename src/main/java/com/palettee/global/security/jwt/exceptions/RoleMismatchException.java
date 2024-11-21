package com.palettee.global.security.jwt.exceptions;

import com.palettee.global.exception.*;

public class RoleMismatchException extends PaletteException {

    public static final PaletteException EXCEPTION = new RoleMismatchException();

    private RoleMismatchException() {
        super(ErrorCode.ROLE_MISMATCH);
    }
}
