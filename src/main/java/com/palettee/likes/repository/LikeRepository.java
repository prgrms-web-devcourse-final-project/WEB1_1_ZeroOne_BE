package com.palettee.likes.repository;

import com.palettee.likes.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    @Query("select count(l.likeId) from Likes l where l.likeType = 'ARCHIVE' and l.targetId = :archiveId")
    long countArchiveLike(@Param("archiveId") Long archiveId);
}
