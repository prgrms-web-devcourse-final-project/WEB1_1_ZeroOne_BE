package com.palettee.archive.exception;

import com.palettee.global.exception.ErrorCode;
import com.palettee.global.exception.PaletteException;

public class NotCommentOwner extends PaletteException {

    public static final NotCommentOwner EXCEPTION = new NotCommentOwner();

    public NotCommentOwner() {
        super(ErrorCode.NOT_COMMENT_OWNER);
    }
}
