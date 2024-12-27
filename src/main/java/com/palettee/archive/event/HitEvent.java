package com.palettee.archive.event;

public record HitEvent(
        Long archiveId,
        String email
) {
}
