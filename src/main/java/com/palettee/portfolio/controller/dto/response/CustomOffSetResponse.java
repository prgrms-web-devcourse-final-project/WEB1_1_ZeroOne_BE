package com.palettee.portfolio.controller.dto.response;

import java.util.List;

public record CustomOffSetResponse(
        List<?> content,
        boolean hasNext,
        Long offset,
        int pageSize
) {

    public static CustomOffSetResponse toDto(List<?> content, boolean hasNext, Long offset, int pageSize) {
        return new CustomOffSetResponse(content, hasNext, offset, pageSize);
    }
}
