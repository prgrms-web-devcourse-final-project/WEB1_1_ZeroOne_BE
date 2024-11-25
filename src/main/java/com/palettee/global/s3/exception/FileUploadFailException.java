package com.palettee.global.s3.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class FileUploadFailException extends PaletteException {
    public static final PaletteException EXCEPTION = new FileUploadFailException();

    public FileUploadFailException() {
        super(ErrorCode.FILE_UPLOAD_FAIL);
    }
}
