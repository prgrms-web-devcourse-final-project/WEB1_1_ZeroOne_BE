package com.palettee.chat_room.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class DuplicateLeaveException extends PaletteException {
    public static final PaletteException EXCEPTION = new DuplicateLeaveException();

    public DuplicateLeaveException() {
        super(ErrorCode.DUPLICATE_LEAVE);
    }
}
