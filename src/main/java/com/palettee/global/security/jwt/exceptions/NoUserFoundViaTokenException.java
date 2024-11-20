package com.palettee.global.security.jwt.exceptions;

import com.palettee.global.exception.*;

public class NoUserFoundViaTokenException extends PaletteException {

    public static final PaletteException Exception = new NoUserFoundViaTokenException();

    private NoUserFoundViaTokenException() {
        super(ErrorCode.NO_USER_FOUND_VIA_TOKEN);
    }
}
