package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {

    @Query("select a from Archive a where a.user.userId = :userId order by a.order desc")
    List<Archive> getAllMyArchive(@Param("userId") Long userId);
}
