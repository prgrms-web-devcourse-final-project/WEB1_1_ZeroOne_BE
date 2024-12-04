package com.palettee.user.controller.dto.response.users;

import com.palettee.user.domain.*;

public record SimpleUserResponse(
        Long userId,
        String name,
        String imageUrl,
        UserRole role
) {

    public static SimpleUserResponse of(User user) {
        return new SimpleUserResponse(
                user.getId(), user.getName(), user.getImageUrl(), user.getUserRole()
        );
    }
}
