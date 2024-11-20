package com.palettee.chat_room.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class ChatRoomNotFoundException extends PaletteException {
    public static final PaletteException EXCEPTION = new ChatRoomNotFoundException();

    public ChatRoomNotFoundException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
