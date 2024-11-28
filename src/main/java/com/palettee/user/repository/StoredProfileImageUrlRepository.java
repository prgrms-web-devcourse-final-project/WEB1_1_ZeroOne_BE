package com.palettee.user.repository;

import com.palettee.user.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface StoredProfileImageUrlRepository
        extends JpaRepository<StoredProfileImageUrl, Long> {

    @Query("select url from StoredProfileImageUrl url where url.user.id = :userId")
    List<StoredProfileImageUrl> findAllByUserId(Long userId);
}
