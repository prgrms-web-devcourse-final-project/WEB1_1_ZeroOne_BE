package com.palettee.user.repository;

import com.palettee.user.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface RelatedLinkRepository
        extends JpaRepository<RelatedLink, Long> {

    @Query("select r from RelatedLink r where r.user.id = :userId")
    List<RelatedLink> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from RelatedLink rl where rl.user.id = :userId")
    void deleteAllByUserId(Long userId);

}
