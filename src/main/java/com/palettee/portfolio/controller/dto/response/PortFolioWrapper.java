package com.palettee.portfolio.controller.dto.response;

import java.util.List;

public record PortFolioWrapper(
        List<PortFolioPopularResponse> portfolioResponses
) {
}
