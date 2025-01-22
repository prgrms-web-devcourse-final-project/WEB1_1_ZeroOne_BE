package com.palettee.chat_room.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class DuplicateCreateException extends PaletteException {
    public static final PaletteException EXCEPTION = new DuplicateCreateException();

    public DuplicateCreateException() {
        super(ErrorCode.DUPLICATE_CREATE);
    }
}
