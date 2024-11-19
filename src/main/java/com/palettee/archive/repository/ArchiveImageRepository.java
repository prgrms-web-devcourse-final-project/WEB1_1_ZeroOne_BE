package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ArchiveImageRepository extends JpaRepository<ArchiveImage, Long> {

    @Query("select ai.imageUrl from ArchiveImage ai where ai.archive.id = :archiveId")
    List<String> findByArchiveId(@Param("archiveId") Long archiveId);
}
