package com.palettee.chat.controller.dto.request;

import com.palettee.chat.controller.dto.response.ChatImgUrl;

import java.util.List;

public record ChatRequest(
        String content,
        List<ChatImgUrl> imgUrls
) {
}
