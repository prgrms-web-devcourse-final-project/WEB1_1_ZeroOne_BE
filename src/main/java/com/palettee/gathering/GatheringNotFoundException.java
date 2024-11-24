package com.palettee.gathering;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class GatheringNotFoundException extends PaletteException {

    public static final PaletteException EXCEPTION = new GatheringNotFoundException();

    private GatheringNotFoundException() {
        super(ErrorCode.GATHERING_NOT_FOUND);

    }
}
