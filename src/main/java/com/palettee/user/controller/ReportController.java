package com.palettee.user.controller;

import com.palettee.global.security.validation.*;
import com.palettee.user.controller.dto.request.reports.*;
import com.palettee.user.controller.dto.response.reports.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    /**
     * 새로운 제보를 등록
     */
    @PostMapping
    public ReportResponse registerReport(
            @RequestBody RegisterReportRequest request
    ) {
        return reportService.registerReport(request, getUserFromContext());
    }

    /**
     * 제보에 새로운 댓글을 등록
     *
     * @param reportId 제보 id
     * @throws ReportNotFoundException id 에 해당하는 제보를 못 찾았을 때
     */
    @PostMapping("/{reportId}")
    public ReportCommentResponse registerReportComment(
            @PathVariable("reportId") Long reportId,
            @RequestBody RegisterReportCommentRequest request
    ) throws ReportNotFoundException {
        return reportService.registerComment(reportId, request, getUserFromContext());
    }

    /**
     * 제보 목록 보여주기
     *
     * @param sort     정렬 기준 {@code (latest, oldest)}
     * @param type     가져올 제보 타입 {@code (bug, enhance, other, all)}
     * @param include  해결된 제보도 가져올지 여부 {@code (fixed, unfixed, all)}
     * @param pageable 페이징 파람 {@code (page & size)}
     * @see com.palettee.user.repository.CustomReportRepository#findReportsWithConditions
     */
    @GetMapping
    public ReportListResponse getReports(
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "include", required = false) String include,
            Pageable pageable
    ) {
        return reportService.getReports(sort, type, include, pageable);
    }


    /**
     * 등록된 제보 세부 내용을 조회
     *
     * @param reportId 제보 id
     * @throws ReportNotFoundException id 에 해당하는 제보를 못 찾았을 때
     */
    @GetMapping("/{reportId}")
    public ReportDetailResponse getReportDetail(
            @PathVariable("reportId") Long reportId
    ) throws ReportNotFoundException {
        return reportService.getReportDetail(reportId);
    }

    /**
     * 특정 제보를 해결함으로 변경
     *
     * @param reportId 제보 id
     * @throws ReportNotFoundException id 에 해당하는 제보를 못 찾았을 때
     */
    @PatchMapping("/{reportId}/fixed")
    public ReportResponse fixReport(
            @PathVariable("reportId") Long reportId
    ) throws ReportNotFoundException {
        return reportService.fixReport(reportId, getUserFromContext());
    }

    private User getUserFromContext() {
        return UserUtils.getContextUser();
    }
}
