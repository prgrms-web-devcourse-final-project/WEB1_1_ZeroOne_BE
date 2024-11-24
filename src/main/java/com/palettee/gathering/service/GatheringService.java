package com.palettee.gathering.service;

import com.palettee.gathering.GatheringNotFoundException;
import com.palettee.gathering.controller.dto.Request.GatheringCreateRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCreateResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;


    @Transactional
    public GatheringCreateResponse createGathering(GatheringCreateRequest request, User user) {
        Gathering gathering = Gathering.builder()
                .user(user)
                .period(request.period())
                .sort(Sort.findSort(request.sort()))
                .subject(Subject.finSubject(request.subject()))
                .contact(Contact.findContact(request.contact()))
                .deadLine(GatheringCreateRequest.getDeadLineLocalDate(request.deadLine()))
                .personnel(request.personnel())
                .position(Position.findPosition(request.position()))
                .title(request.title())
                .content(request.content())
                .url(request.url())
                .gatheringTagList(GatheringCreateRequest.getGatheringTag(request.gatheringTag()))
                .build();

        return GatheringCreateResponse.toDTO(gatheringRepository.save(gathering));
    }

    public Slice<GatheringResponse> findAll(
            String sort,
            String period,
            String position,
            String status,
            Long gatheringId,
            Pageable pageable
    ) {
        return gatheringRepository.pageGathering(
                sort,
                period,
                position,
                status,
                gatheringId,
                pageable
        );
    }

    public GatheringDetailsResponse findByDetails(Long gatheringId) {
        Gathering gathering = getGathering(gatheringId);

        return GatheringDetailsResponse.toDto(gathering);
    }

    @Transactional
    public GatheringCreateResponse updateGathering(Long gatheringId, GatheringCreateRequest request, User user) {

        if(!gatheringRepository.existsByUserId(user.getId())) {
           throw  UserAccessException.EXCEPTION;
        }

        Gathering gathering = gatheringRepository.findByFetchId(gatheringId).orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

        gathering.updateGathering(request);

        return GatheringCreateResponse.toDTO(gathering);
    }

    private Gathering getGathering(Long gatheringId) {
        return gatheringRepository.findByGatheringId(gatheringId)
                .orElseThrow(() -> GatheringNotFoundException.EXCEPTION);
    }
}
