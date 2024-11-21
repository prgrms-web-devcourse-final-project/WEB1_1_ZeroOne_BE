package com.palettee.archive.controller.dto.response;

import org.springframework.data.domain.Slice;

public record SliceInfo(
        int currentPage,
        int size,
        boolean hasNext
) {

    public static SliceInfo of(Slice<?> slice) {
        return new SliceInfo(slice.getNumber(), slice.getSize(), slice.hasNext());
    }

}
