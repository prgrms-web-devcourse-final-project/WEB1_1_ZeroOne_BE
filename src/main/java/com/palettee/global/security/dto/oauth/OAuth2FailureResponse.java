package com.palettee.global.security.dto.oauth;

import java.time.*;

public record OAuth2FailureResponse(int status, String reason, LocalDateTime timeStamp) {

}
