package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select count(c.id) from Comment c where c.archive.id = :archiveId")
    long countArchiveComment(@Param("archiveId") Long archiveId);

    @Query("select c from Comment c where c.archive.id = :archiveId")
    Slice<Comment> findCommentWithArchiveId(@Param("archiveId") Archive archive, Pageable pageable);
}
