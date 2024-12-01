package com.palettee.user.repository;

import com.palettee.user.domain.*;
import org.springframework.data.domain.*;

public interface CustomReportRepository {

    /**
     * 페이지 요청에 따라 제보들을 보여주는 메서드
     *
     * @param pageable 페이징
     * @param sort     정렬 기준 {@code (latest, oldest)}
     * @param type     보여줄 제보 타입 {@code (bug, enhance, other, all)}
     * @param include  해결된 제보도 보여줄지 여부 {@code (fixed, unfixed, all)}
     */
    Slice<Report> findReportsWithConditions(
            Pageable pageable, String sort, String type, String include
    );
}
