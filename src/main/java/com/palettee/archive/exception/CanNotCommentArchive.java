package com.palettee.archive.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class CanNotCommentArchive extends PaletteException {

    public static final CanNotCommentArchive EXCEPTION = new CanNotCommentArchive();

    public CanNotCommentArchive() {
        super(ErrorCode.COMMENT_NOT_OPEN);
    }
}
