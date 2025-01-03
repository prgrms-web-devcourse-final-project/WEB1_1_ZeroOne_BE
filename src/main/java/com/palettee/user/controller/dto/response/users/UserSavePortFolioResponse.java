package com.palettee.user.controller.dto.response.users;

import com.palettee.portfolio.domain.PortFolio;
import com.palettee.user.domain.User;

public record UserSavePortFolioResponse(Long userId, Long portFolioId) {

    public static UserSavePortFolioResponse of(User user, PortFolio portFolio) {
        return new UserSavePortFolioResponse(user.getId(), portFolio.getPortfolioId());
    }
}
