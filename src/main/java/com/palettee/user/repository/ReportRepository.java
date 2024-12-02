package com.palettee.user.repository;

import com.palettee.user.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ReportRepository
        extends JpaRepository<Report, Long>, CustomReportRepository {

    @Query("select re from Report re left join fetch re.reportComments "
            + "where re.id = :reportId")
    Optional<Report> findByIdFetchWithComments(Long reportId);
}
