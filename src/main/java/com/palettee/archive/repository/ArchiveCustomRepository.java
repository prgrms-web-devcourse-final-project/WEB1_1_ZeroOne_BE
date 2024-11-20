package com.palettee.archive.repository;

import com.palettee.archive.domain.Archive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ArchiveCustomRepository {

    Slice<Archive> findAllArchiveWithCondition(String category, Pageable pageable);

}
