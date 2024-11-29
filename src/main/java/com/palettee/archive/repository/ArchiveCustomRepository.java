package com.palettee.archive.repository;

import com.palettee.archive.domain.*;
import com.palettee.user.controller.dto.response.*;
import java.util.*;
import org.springframework.data.domain.*;

public interface ArchiveCustomRepository {

    Slice<Archive> findAllArchiveWithCondition(String majorJObGroup, String minorJobGroup, String sort, Pageable pageable);

    Slice<Archive> searchArchive(String searchKeyword, List<Long> ids, Pageable pageable);

    /**
     * {@code NoOffset} 방식을 이용한 {@code 특정 유저의 아카이브} 목록 조회 메서드
     * <p>
     * {@code (ID <= archiveOffset)} 인 {@code archive} 들을 최신순 정렬 {@code (archiveId 내림차순)} 로 가져옴.
     *
     * @param userId        조회할 유저의 id
     * @param size          가져올 아카이브 개수
     * @param archiveOffset 이전 조회에서 제공된 {@code nextArchiveId}
     */
    GetUserArchiveResponse findArchivesOnUserWithNoOffset(Long userId, int size,
            Long archiveOffset);
}
