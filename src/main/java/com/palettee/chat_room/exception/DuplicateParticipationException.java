package com.palettee.chat_room.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class DuplicateParticipationException extends PaletteException {
    public static final PaletteException EXCEPTION = new DuplicateParticipationException();

    public DuplicateParticipationException() {
        super(ErrorCode.DUPLICATE_PARTICIPATION);
    }
}
