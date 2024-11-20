package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {

}
