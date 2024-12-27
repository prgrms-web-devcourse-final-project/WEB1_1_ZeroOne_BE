package com.palettee.archive.repository;

import com.palettee.archive.controller.dto.response.*;
import com.palettee.archive.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {

    @Query("select a from Archive a where a.user.id = :userId order by a.archiveOrder desc")
    Slice<Archive> getAllMyArchive(@Param("userId") Long userId, Pageable pageable);

    @Query("select a from Archive a where a.id in :ids order by a.archiveOrder desc")
    Slice<Archive> findAllInIds(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("select a from Archive a order by a.hits desc, a.id desc limit 5")
    List<Archive> getMainArchives();

    @Query("SELECT a.type AS type, COUNT(a) AS count FROM Archive a GROUP BY a.type")
    List<ColorCount> countByArchiveType();

    @Query("SELECT a.type AS type, COUNT(a) AS count FROM Archive a where a.user.id = :userId GROUP BY a.type")
    List<ColorCount> countMyArchiveByArchiveType(@Param("userId") Long userId);

    @Query("SELECT a.type AS type, COUNT(a) AS count FROM Archive a where a.id in :ids GROUP BY a.type")
    List<ColorCount> countLikeArchiveByArchiveType(@Param("ids") List<Long> ids);

    @Query("SELECT a FROM Archive a where a.user.id = :userId")
    List<Archive> findAllByUserId(Long userId);

    @Modifying
    @Query("UPDATE Archive a SET a.hits = :hitCount WHERE a.id = :archiveId")
    void updateHitCount(@Param("archiveId") Long archiveId, @Param("hitCount") Long hitCount);

    @Query("select a from Archive a order by (a.hits + a.likeCount) desc")
    List<Archive> findTopArchives(Pageable pageable);
}
