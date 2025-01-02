package com.palettee.archive.event;

public record LikeEvent(
        Long archiveId,
        Long userId
) {
}
