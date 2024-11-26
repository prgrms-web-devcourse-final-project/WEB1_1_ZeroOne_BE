package com.palettee.global.s3.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class FileEmptyException extends PaletteException {
    public static final FileEmptyException EXCEPTION = new FileEmptyException();

    public FileEmptyException() {
        super(ErrorCode.NO_EXIST_FILE);
    }
}
