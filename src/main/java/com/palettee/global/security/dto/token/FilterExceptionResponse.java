package com.palettee.global.security.dto.token;

import java.time.*;

public record FilterExceptionResponse(int status, String reason, LocalDateTime timeStamp) {

}
