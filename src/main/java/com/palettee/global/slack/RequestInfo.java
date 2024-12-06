package com.palettee.global.slack;

public record RequestInfo(
        String requestUrl,
        String method,
        String remoteAddr
) {
}
