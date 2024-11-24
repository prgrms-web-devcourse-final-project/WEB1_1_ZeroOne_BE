package com.palettee.user.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class UserAccessException extends PaletteException {

    public static final PaletteException EXCEPTION = new UserAccessException();

    public UserAccessException() {
        super(ErrorCode.NOT_ACCESS);
    }

}
