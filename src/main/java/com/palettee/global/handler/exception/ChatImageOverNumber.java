package com.palettee.global.handler.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class ChatImageOverNumber extends PaletteException {
    public static final PaletteException EXCEPTION = new ChatImageOverNumber();

    public ChatImageOverNumber() {
        super(ErrorCode.CHAT_IMAGE_NUMBER_OVER);
    }
}
