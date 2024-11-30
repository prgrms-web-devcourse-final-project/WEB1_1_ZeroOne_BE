package com.palettee.user.repository;

import com.palettee.user.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthIdentity(String oauthIdentity);

    @Query("select u.name from User u where u.id = :targetId")
    String getUsername(@Param("targetId") Long targetId);

    @Query("select u from User u left join fetch u.relatedLinks where u.id = :userId")
    Optional<User> findByIdFetchWithRelatedLinks(Long userId);

    @Query("select u from User u left join fetch u.portfolios where u.id = :userId")
    Optional<User> findByIdFetchWithPortfolios(Long userId);

    @Query("select u from User u left join fetch u.storedProfileImageUrls where u.id = :userId")
    Optional<User> findByIdFetchWithStoredProfileUrls(Long userId);
}
