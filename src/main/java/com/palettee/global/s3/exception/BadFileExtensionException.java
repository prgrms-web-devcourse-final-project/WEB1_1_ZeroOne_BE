package com.palettee.global.s3.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class BadFileExtensionException extends PaletteException {
    public static final PaletteException EXCEPTION = new BadFileExtensionException();


    public BadFileExtensionException() {
        super(ErrorCode.BAD_FILE_EXTENSION);
    }
}
