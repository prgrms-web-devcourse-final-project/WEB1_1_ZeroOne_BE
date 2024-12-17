package com.palettee.portfolio.controller.dto.response;

import java.util.List;

public record CustomPortFolioResponse(
        List<PortFolioResponse> content,
        boolean hasNext,
        Long nextId
) {

    public static CustomPortFolioResponse toDTO(List<PortFolioResponse> content, boolean hasNext, Long nextLikeId) {
        return new CustomPortFolioResponse (content, hasNext, nextLikeId);
    }
}
