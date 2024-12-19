package com.palettee.gathering.event;

import com.palettee.gathering.domain.Gathering;

public record GatheringDeleteEventListener(
        Gathering gathering
) {
}
