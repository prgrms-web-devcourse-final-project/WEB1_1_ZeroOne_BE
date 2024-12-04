package com.palettee.likes.controller.dto;

import com.palettee.likes.domain.LikeType;

public record LikeDto(
        Long targetId,
        Long userId,
        LikeType likeType
) {
}
