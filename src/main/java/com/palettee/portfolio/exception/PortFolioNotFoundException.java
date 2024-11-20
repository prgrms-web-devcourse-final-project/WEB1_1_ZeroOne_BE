package com.palettee.portfolio.exception;

import com.palettee.chat_room.exception.ChatRoomNotFoundException;
import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class PortFolioNotFoundException extends PaletteException {

    public static final PaletteException EXCEPTION = new PortFolioNotFoundException();


    public PortFolioNotFoundException() {
        super(ErrorCode.PORT_FOLIO_NOT_FOUND);
    }
}
