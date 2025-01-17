package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;
import com.palettee.archive.repository.ArchiveImageRepository;
import java.util.*;

public record GetUserArchiveResponse(
        List<SimpleArchiveInfo> archives,
        boolean hasNext,
        Long nextArchiveId
) {

    public static GetUserArchiveResponse of(
            List<Archive> archivesList,
            boolean hasNext, Long nextArchiveId, ArchiveImageRepository archiveImageRepository) {

        List<SimpleArchiveInfo> archives
                = archivesList.stream()
                .map(it -> SimpleArchiveInfo.of(it, archiveImageRepository.getArchiveThumbnail(it.getId())))
                .toList();

        return new GetUserArchiveResponse(archives, hasNext, nextArchiveId);
    }


}
