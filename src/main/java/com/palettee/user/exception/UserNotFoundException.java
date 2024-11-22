package com.palettee.user.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class UserNotFoundException extends PaletteException {

    public static final PaletteException EXCEPTION = new UserNotFoundException();

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
