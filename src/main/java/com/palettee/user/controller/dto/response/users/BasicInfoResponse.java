package com.palettee.user.controller.dto.response.users;

import com.palettee.user.domain.*;

public record BasicInfoResponse(
        String email, String name, String imageUrl
) {

    public static BasicInfoResponse of(User user) {
        return new BasicInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getImageUrl()
        );
    }
}
