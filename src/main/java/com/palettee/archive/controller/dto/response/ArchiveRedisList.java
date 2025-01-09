package com.palettee.archive.controller.dto.response;

import java.util.List;

public record ArchiveRedisList(
        List<ArchiveRedisResponse> archives
) {
}
