package com.palettee.global.handler.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class ChatContentNullException extends PaletteException {
    public final static PaletteException EXCEPTION = new ChatContentNullException();

    public ChatContentNullException() {
        super(ErrorCode.CHAT_CONTENT_NULL);
    }
}
