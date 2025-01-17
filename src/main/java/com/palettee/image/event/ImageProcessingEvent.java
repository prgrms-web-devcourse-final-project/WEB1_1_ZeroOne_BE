package com.palettee.image.event;

import com.palettee.image.ContentType;

public record ImageProcessingEvent(
        String content,
        Long targetId,
        ContentType contentType
) {
}
