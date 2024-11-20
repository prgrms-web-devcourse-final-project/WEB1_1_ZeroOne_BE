package com.palettee.chat.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class ChatUserNotFoundException extends PaletteException {
    public static final PaletteException EXCEPTION = new ChatUserNotFoundException();

    public ChatUserNotFoundException() {
        super(ErrorCode.CHAT_USER_NOT_FOUND);
    }
}
