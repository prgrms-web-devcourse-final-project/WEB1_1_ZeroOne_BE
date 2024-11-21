package com.palettee.archive.controller.dto.response;

import java.util.List;

public record CommentListResponse(
        List<CommentDetail> comments,
        SliceInfo meta
) {
}
