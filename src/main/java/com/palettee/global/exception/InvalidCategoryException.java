package com.palettee.global.exception;

public class InvalidCategoryException extends  PaletteException{

    public static final PaletteException EXCEPTION = new InvalidCategoryException();


    public InvalidCategoryException() {
        super(ErrorCode.CATEGORY_NOT_FOUND);
    }
}
