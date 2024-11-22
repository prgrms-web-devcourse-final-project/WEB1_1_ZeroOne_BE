package com.palettee.likes.repository;

import com.palettee.likes.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    @Query("select count(l.likeId) from Likes l where l.likeType = 'ARCHIVE' and l.targetId = :archiveId")
    long countArchiveLike(@Param("archiveId") Long archiveId);

    @Query("select l.targetId from Likes l where l.user.id = :userId and l.likeType = 'ARCHIVE'")
    List<Long> findMyLikeList(@Param("userId") Long userId);

    Likes findByUserIdAndTargetId(Long userId, Long targetId);
}
