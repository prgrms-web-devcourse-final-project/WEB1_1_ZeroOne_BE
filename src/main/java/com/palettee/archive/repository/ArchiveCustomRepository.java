package com.palettee.archive.repository;

import com.palettee.archive.domain.Archive;
import java.util.List;

public interface ArchiveCustomRepository {

    List<Archive> findAllArchiveWithCondition();

}
