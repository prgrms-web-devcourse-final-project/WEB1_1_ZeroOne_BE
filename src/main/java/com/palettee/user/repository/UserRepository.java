package com.palettee.user.repository;

import com.palettee.user.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthIdentity(String oauthIdentity);

    @Query("select u.name from User u where u.id = :targetId")
    String getUsername(@Param("targetId") Long targetId);
}
