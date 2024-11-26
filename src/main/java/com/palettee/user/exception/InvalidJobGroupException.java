package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class InvalidJobGroupException extends PaletteException {

    public static PaletteException EXCEPTION = new InvalidJobGroupException();

    public InvalidJobGroupException() {
        super(ErrorCode.INVALID_JOB_GROUP);
    }
}
