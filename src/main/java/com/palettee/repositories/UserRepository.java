package com.palettee.repositories;

import com.palettee.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UserRepository
        extends JpaRepository<User, Long> {

}
