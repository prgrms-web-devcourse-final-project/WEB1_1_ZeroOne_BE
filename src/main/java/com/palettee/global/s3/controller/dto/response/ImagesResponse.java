package com.palettee.global.s3.controller.dto.response;

import java.util.List;

public record ImagesResponse(
        List<ImageUrlResponse> imgUrls
) {
}
