package com.palettee.global.handler.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class ChatContentOverLength extends PaletteException {
    public static final PaletteException EXCEPTION = new ChatContentOverLength();

    public ChatContentOverLength() {
        super(ErrorCode.CHAT_OVER_LENGTH);
    }
}
