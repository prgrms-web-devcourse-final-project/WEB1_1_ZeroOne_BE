package com.palettee.portfolio.controller.dto.response;

import java.util.List;

public record CustomSliceResponse(
        List<?> content,
        boolean hasNext,
        Long nextLikeId
) {

    public static CustomSliceResponse toDTO(List<?> content, boolean hasNext, Long nextLikeId) {
        return new CustomSliceResponse(content, hasNext, nextLikeId);
    }
}
