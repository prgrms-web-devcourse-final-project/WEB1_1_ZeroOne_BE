package com.palettee.global.handler.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class JSONMappingException extends PaletteException {
    public final static PaletteException EXCEPTION = new JSONMappingException();

    public JSONMappingException() {
        super(ErrorCode.JSON_MAPPING_ERROR);
    }
}
