package com.palettee.notification.exception;

import static com.palettee.global.exception.ErrorCode.NOTIFICATION_NOT_FOUND;

import com.palettee.global.exception.PaletteException;

public class NotificationNotFound extends PaletteException {

    public static final NotificationNotFound EXCEPTION = new NotificationNotFound();

    public NotificationNotFound() {
        super(NOTIFICATION_NOT_FOUND);
    }
}
