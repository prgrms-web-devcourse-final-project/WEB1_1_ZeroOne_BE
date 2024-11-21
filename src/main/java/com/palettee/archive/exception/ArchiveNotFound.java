package com.palettee.archive.exception;

import static com.palettee.global.exception.ErrorCode.ARCHIVE_NOT_FOUND_EXCEPTION;

import com.palettee.global.exception.PaletteException;

public class ArchiveNotFound extends PaletteException {

    public final static ArchiveNotFound EXCEPTION = new ArchiveNotFound();

    public ArchiveNotFound() {
        super(ARCHIVE_NOT_FOUND_EXCEPTION);
    }
}
