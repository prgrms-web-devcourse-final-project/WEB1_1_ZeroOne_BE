package com.palettee.notification.exception;

import static com.palettee.global.exception.ErrorCode.NOT_MY_NOTIFICATION;

import com.palettee.global.exception.PaletteException;

public class NotMyNotification extends PaletteException {

    public static final NotificationNotFound EXCEPTION = new NotificationNotFound();

    public NotMyNotification() {
        super(NOT_MY_NOTIFICATION);
    }
}
