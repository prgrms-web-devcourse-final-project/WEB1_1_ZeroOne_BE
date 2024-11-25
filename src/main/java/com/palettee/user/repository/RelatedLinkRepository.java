package com.palettee.user.repository;

import com.palettee.user.domain.*;
import org.springframework.data.jpa.repository.*;

public interface RelatedLinkRepository
        extends JpaRepository<RelatedLink, Long> {

    @Modifying
    @Query("delete from RelatedLink rl where rl.user.id = :userId")
    void deleteAllByUserId(Long userId);

}
