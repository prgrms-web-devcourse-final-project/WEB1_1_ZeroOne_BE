package com.palettee.likes.repository;

import com.palettee.likes.domain.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    @Query("select count(l.likeId) from Likes l where l.likeType = 'ARCHIVE' and l.targetId = :archiveId")
    long countArchiveLike(@Param("archiveId") Long archiveId);

    @Query("select l.targetId from Likes l where l.user.id = :userId and l.likeType = 'ARCHIVE'")
    List<Long> findMyLikeList(@Param("userId") Long userId);

    @Query("select l from Likes l where l.user.id = :userId and l.targetId = :targetId and l.likeType = :likeType")
    Likes findByUserIdAndTargetId(Long userId, Long targetId, LikeType likeType);

    @Query("select a FROM Likes a WHERE a.targetId = :id AND a.user.id = :userId and a.likeType = 'ARCHIVE'")
    Optional<Likes> existByUserAndArchive(@Param("id") Long id, @Param("userId") Long userId);
  
    @Query("select l from Likes l where l.targetId = :targetId and l.likeType = 'PORTFOLIO'")
    List<Likes> findByTargetId(Long targetId);

    @Query("select l from Likes l where l.user.id = :userId and l.targetId = :targetId and l.likeType = :likeType")
    List<Likes> findByList(Long userId, Long targetId, LikeType likeType);

    @Query("select count(l) from Likes l where l.targetId = :targetId and l.likeType = 'GATHERING'")
    long countByTargetId(Long targetId);





    @Modifying
    @Query("delete from Likes l where l.likeId in :likeIds")
    void deleteAllBy(@Param("likeIds") List<Long> likeIds);

}
