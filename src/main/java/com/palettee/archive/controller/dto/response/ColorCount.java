package com.palettee.archive.controller.dto.response;

import com.palettee.archive.domain.ArchiveType;

public interface ColorCount {
    ArchiveType getType();
    Long getCount();
}
