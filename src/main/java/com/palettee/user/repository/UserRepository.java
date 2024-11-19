package com.palettee.user.repository;

import com.palettee.user.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserRepository
        extends JpaRepository<User, Long> {

}
