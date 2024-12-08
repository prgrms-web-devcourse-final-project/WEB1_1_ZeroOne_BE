package com.palettee.archive.controller.dto.response;

import java.util.List;

public record ArchiveListResponse(
        List<ArchiveSimpleResponse> archives,
        List<ColorCount> colorCount,
        SliceInfo meta
) {
}
