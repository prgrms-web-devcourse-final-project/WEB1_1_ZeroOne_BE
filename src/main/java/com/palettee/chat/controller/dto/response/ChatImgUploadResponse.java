package com.palettee.chat.controller.dto.response;

import java.util.List;

public record ChatImgUploadResponse(
        List<ChatImgUrlResponse> imgUrls
) {
}
