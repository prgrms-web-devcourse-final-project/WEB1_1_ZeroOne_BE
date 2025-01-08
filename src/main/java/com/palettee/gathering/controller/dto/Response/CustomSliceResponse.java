package com.palettee.gathering.controller.dto.Response;

import java.util.List;

public record CustomSliceResponse(
        List<GatheringResponse> content,
        boolean hasNext,
        Long nextId
) {

    public static  CustomSliceResponse toDTO(List<GatheringResponse> content, boolean hasNext, Long nextLikeId) {
        return new CustomSliceResponse (content, hasNext, nextLikeId);
    }
}
