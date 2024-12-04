package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {

    @Query("select a from Archive a where a.user.id = :userId order by a.archiveOrder desc")
    List<Archive> getAllMyArchive(@Param("userId") Long userId);

    @Query("select a from Archive a where a.id in :ids order by a.archiveOrder desc")
    List<Archive> findAllInIds(@Param("ids") List<Long> ids);

    @Query("select a from Archive a order by a.id desc limit 5")
    List<Archive> getMainArchives();

}
