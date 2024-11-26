package com.palettee.global.handler.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class WrongSubPathException extends PaletteException {
    public final static PaletteException EXCEPTION = new WrongSubPathException();

    public WrongSubPathException() {
        super(ErrorCode.WRONG_SUB_PATH);
    }
}
