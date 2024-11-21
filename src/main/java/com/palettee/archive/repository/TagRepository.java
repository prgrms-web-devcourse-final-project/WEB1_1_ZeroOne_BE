package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("select t.content from Tag t where t.archive.id = :archiveId")
    List<String> findByArchiveId(@Param("archiveId") Long archiveId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Tag t where t.archive.id = :archiveId")
    void deleteAllByArchiveId(@Param("archiveId") Long archiveId);

    @Query("select t.archive.id from Tag t where t.content = :searchKeyword")
    List<Long> findAllArchiveIds(@Param("searchKeyword") String searchKeyword);

}
