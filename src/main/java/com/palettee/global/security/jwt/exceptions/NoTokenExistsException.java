package com.palettee.global.security.jwt.exceptions;

import com.palettee.global.exception.*;

public class NoTokenExistsException extends PaletteException {

    public static final PaletteException EXCEPTION = new NoTokenExistsException();

    private NoTokenExistsException() {
        super(ErrorCode.NO_TOKEN_EXISTS);
    }
}
