package com.palettee.archive.exception;

import static com.palettee.global.exception.ErrorCode.COMMENT_NOT_FOUND;

import com.palettee.global.exception.PaletteException;

public class CommentNotFound extends PaletteException {

    public static final CommentNotFound EXCEPTION = new CommentNotFound();

    public CommentNotFound() {
        super(COMMENT_NOT_FOUND);
    }
}
