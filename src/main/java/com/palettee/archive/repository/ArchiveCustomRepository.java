package com.palettee.archive.repository;

import com.palettee.archive.domain.Archive;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ArchiveCustomRepository {

    Slice<Archive> findAllArchiveWithCondition(String majorJObGroup, String minorJobGroup, String sort, Pageable pageable);

    Slice<Archive> searchArchive(String searchKeyword, List<Long> ids, Pageable pageable);
}
