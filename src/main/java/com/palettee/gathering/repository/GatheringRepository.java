package com.palettee.gathering.repository;

import com.palettee.gathering.domain.Gathering;
import org.springframework.data.jpa.repository.*;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {

}
