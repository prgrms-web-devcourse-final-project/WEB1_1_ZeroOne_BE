package com.palettee.user.controller.dto.response.users;

import com.palettee.user.domain.*;

public record UserResponse(
        Long userId
) {

    public static UserResponse of(User user) {
        return new UserResponse(user.getId());
    }
}
