package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;
import java.util.*;

public record GetUserArchiveResponse(
        List<SimpleArchiveInfo> archives,
        boolean hasNext,
        Long nextArchiveId
) {

    public static GetUserArchiveResponse of(
            List<Archive> archivesList,
            boolean hasNext, Long nextArchiveId) {

        List<SimpleArchiveInfo> archives
                = archivesList.stream()
                .map(SimpleArchiveInfo::of)
                .toList();

        return new GetUserArchiveResponse(archives, hasNext, nextArchiveId);
    }


}
