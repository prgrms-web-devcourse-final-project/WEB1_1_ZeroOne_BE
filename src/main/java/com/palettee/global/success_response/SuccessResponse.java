package com.palettee.global.success_response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SuccessResponse {

    private final Object data;
    private final LocalDateTime timeStamp;

    public SuccessResponse(Object data) {
        this.data = data;
        this.timeStamp = LocalDateTime.now();
    }
}
