package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class NotOwnUserException extends PaletteException {

    public static PaletteException EXCEPTION = new NotOwnUserException();

    public NotOwnUserException() {
        super(ErrorCode.NOT_OWN_USER);
    }
}
