package com.palettee.archive.repository;

import com.palettee.archive.controller.dto.response.ColorCount;
import com.palettee.archive.domain.*;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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

}
