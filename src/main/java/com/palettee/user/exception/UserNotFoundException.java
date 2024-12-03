package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class UserNotFoundException extends PaletteException {

    public static final PaletteException EXCEPTION = new UserNotFoundException();

    private UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
