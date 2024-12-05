package com.palettee.archive.exception;

import static com.palettee.global.exception.ErrorCode.NOT_ARCHIVE_OWNER;

import com.palettee.global.exception.PaletteException;

public class NotArchiveOwner extends PaletteException {

    public static final NotArchiveOwner EXCEPTION = new NotArchiveOwner();

    public NotArchiveOwner() {
        super(NOT_ARCHIVE_OWNER);
    }
}
