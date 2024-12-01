package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class UserAccessException extends PaletteException {

    public static final PaletteException EXCEPTION = new UserAccessException();

    private UserAccessException() {
        super(ErrorCode.NOT_ACCESS);
    }

}
