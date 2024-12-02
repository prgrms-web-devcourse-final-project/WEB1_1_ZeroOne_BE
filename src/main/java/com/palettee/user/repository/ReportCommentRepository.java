package com.palettee.user.repository;

import com.palettee.user.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

public interface ReportCommentRepository
        extends JpaRepository<ReportComment, Long> {

    @Query("select c from ReportComment c left join fetch c.user where c.report.id = :reportId")
    Slice<ReportComment> findCommentByReportId(
            @Param("reportId") Long reportId, Pageable pageable
    );
}
