package com.palettee.user.service;

import com.palettee.archive.controller.dto.response.*;
import com.palettee.user.controller.dto.request.reports.*;
import com.palettee.user.controller.dto.response.reports.*;
import com.palettee.user.domain.*;
import com.palettee.user.exception.*;
import com.palettee.user.repository.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepo;
    private final ReportCommentRepository reportCommentRepo;

    /**
     * 새로운 제보를 등록
     *
     * @param user 로그인한 유저
     * @throws InvalidReportTypeException {@code request} 의 {@code reportType} 이 이상할 때
     */
    @Transactional
    public ReportResponse registerReport(RegisterReportRequest request, User user)
            throws InvalidReportTypeException {
        ReportType type = ReportType.of(request.reportType());

        Report entity = Report.builder()
                .title(request.title())
                .content(request.content())
                .reportType(type)
                .user(user)
                .build();

        reportRepo.save(entity);

        log.info("New report has been registered on user {}.", user.getId());

        return ReportResponse.of(entity);
    }

    /**
     * 제보에 새로운 댓글을 등록
     *
     * @param reportId 제보 id
     * @param user     로그인한 유저
     * @throws ReportNotFoundException {@code reportId} 에 해당하는 제보를 못 찾았을 때
     */
    @Transactional
    public ReportCommentResponse registerComment(
            Long reportId, RegisterReportCommentRequest request, User user
    ) throws ReportNotFoundException {

        Report report = reportRepo.findByIdFetchWithComments(reportId)
                .orElseThrow(() -> ReportNotFoundException.EXCEPTION);

        ReportComment entity = ReportComment.builder()
                .content(request.content())
                .user(user)
                .report(report)
                .build();

        reportCommentRepo.save(entity);

        log.info("Report comment has been added on Report {} with user {}.",
                reportId, user.getId());

        return ReportCommentResponse.of(entity);
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
    public ReportListResponse getReports(
            String sort, String type, String include, Pageable pageable
    ) {

        Slice<Report> slice = reportRepo.findReportsWithConditions(
                pageable, sort, type, include
        );

        List<SimpleReportResponse> list = slice
                .map(SimpleReportResponse::of)
                .toList();

        return new ReportListResponse(list, SliceInfo.of(slice));
    }

    /**
     * 등록된 제보 세부 내용을 조회
     *
     * @param reportId 제보 id
     */
    public ReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> ReportNotFoundException.EXCEPTION);

        return ReportDetailResponse.of(report);
    }

    /**
     * 특정 제보의 댓글 내용을 조회
     *
     * @param reportId 제보 id
     * @param pageable 페이징 파람 {@code (page & size)}
     * @throws ReportNotFoundException id 에 해당하는 제보를 못 찾았을 때
     */
    public ReportCommentListResponse getReportComments(Long reportId, Pageable pageable)
            throws ReportNotFoundException {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> ReportNotFoundException.EXCEPTION);

        Slice<ReportComment> comments = reportCommentRepo.findCommentByReportId(
                report.getId(), pageable
        );

        List<SimpleReportCommentResponse> resp = comments.getContent()
                .stream()
                .map(SimpleReportCommentResponse::of)
                .toList();

        return new ReportCommentListResponse(resp, SliceInfo.of(comments));
    }

    /**
     * 특정 제보를 해결함으로 변경
     *
     * @param reportId  제보 id
     * @param adminUser 관리자 유저
     * @throws ReportNotFoundException id 에 해당하는 제보를 못 찾았을 때
     */
    @Transactional
    public ReportResponse fixReport(Long reportId, User adminUser)
            throws ReportNotFoundException {

        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> ReportNotFoundException.EXCEPTION);

        if (report.isFixed()) {
            log.warn("Report {} is already been fixed.", reportId);
        } else {
            report.reportFixed();

            // TODO : 나중에 이메일 알림이라도 붙일까?
            log.info("Report {} has been fixed by user {}.", reportId, adminUser.getId());
        }

        return ReportResponse.of(report);
    }
}
