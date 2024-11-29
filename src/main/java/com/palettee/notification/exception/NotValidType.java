package com.palettee.notification.exception;

import static com.palettee.global.exception.ErrorCode.NOT_VALID_ALERT_TYPE;

import com.palettee.global.exception.PaletteException;

public class NotValidType extends PaletteException {

    public static final NotValidType EXCEPTION = new NotValidType();

    public NotValidType() {
        super(NOT_VALID_ALERT_TYPE);
    }
}
