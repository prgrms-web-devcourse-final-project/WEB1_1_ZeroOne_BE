package com.palettee.repositories;

import com.palettee.domain.*;
import org.springframework.data.jpa.repository.*;

public interface GatheringRepository
        extends JpaRepository<Gathering, Integer> {

}
