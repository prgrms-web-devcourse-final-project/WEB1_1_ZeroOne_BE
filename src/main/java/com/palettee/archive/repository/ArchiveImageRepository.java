package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ArchiveImageRepository
        extends JpaRepository<ArchiveImage, Long> {

}
