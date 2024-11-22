package com.palettee.portfolio.controller.dto.response;

import com.palettee.likes.domain.Likes;

public record PortFolioLikeResponse(Long portFolioId) {


    public static PortFolioLikeResponse toDTO(Likes likes){
        return new PortFolioLikeResponse(likes.getLikeId());
    }
}
