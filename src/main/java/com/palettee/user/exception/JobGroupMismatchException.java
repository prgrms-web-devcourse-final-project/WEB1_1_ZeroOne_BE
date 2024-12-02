package com.palettee.user.exception;

import com.palettee.global.exception.*;

public class JobGroupMismatchException extends PaletteException {

    public static PaletteException EXCEPTION = new JobGroupMismatchException();

    private JobGroupMismatchException() {
        super(ErrorCode.JOB_GROUP_MISMATCH);
    }
}
