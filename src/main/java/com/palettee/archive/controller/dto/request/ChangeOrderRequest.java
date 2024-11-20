package com.palettee.archive.controller.dto.request;

import java.util.Map;

public record ChangeOrderRequest(
        Map<Long, Integer> orderRequest
) {
}
