package com.palettee.gathering.service;

import com.palettee.gathering.GatheringNotFoundException;
import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.user.domain.User;
import com.palettee.user.exception.UserAccessException;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
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

    private final UserRepository userRepository;


    @Transactional
    public GatheringCommonResponse createGathering(GatheringCommonRequest request, User user) {

        User findByUser = userRepository.findById(user.getId()).orElseThrow(() -> UserNotFoundException.EXCEPTION);

        Gathering gathering = Gathering.builder()
                .user(findByUser)
                .period(request.period())
                .sort(Sort.findSort(request.sort()))
                .subject(Subject.finSubject(request.subject()))
                .contact(Contact.findContact(request.contact()))
                .deadLine(GatheringCommonRequest.getDeadLineLocalDate(request.deadLine()))
                .personnel(request.personnel())
                .position(Position.findPosition(request.position()))
                .title(request.title())
                .content(request.content())
                .url(request.url())
                .gatheringTagList(GatheringCommonRequest.getGatheringTag(request.gatheringTag()))
                .build();

        return GatheringCommonResponse.toDTO(gatheringRepository.save(gathering));
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
        Gathering gathering = getFetchGathering(gatheringId);

        return GatheringDetailsResponse.toDto(gathering);
    }

    @Transactional
    public GatheringCommonResponse updateGathering(Long gatheringId, GatheringCommonRequest request, User user) {

        accessUser(user);

        Gathering gathering = gatheringRepository.findByFetchId(gatheringId).orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

        gathering.updateGathering(request);

        return GatheringCommonResponse.toDTO(gathering);
    }



    @Transactional
    public GatheringCommonResponse deleteGathering(Long gatheringId, User user) {
        accessUser(user);

        Gathering gathering = getGathering(gatheringId);

        gatheringRepository.delete(gathering);

        return GatheringCommonResponse.toDTO(gathering);
    }

    @Transactional
    public GatheringCommonResponse updateStatusGathering(Long gatheringId, User user){
        accessUser(user);
        Gathering gathering = getGathering(gatheringId);

        gathering.updateStatusComplete();

        return GatheringCommonResponse.toDTO(gathering);
    }

    private Gathering getFetchGathering(Long gatheringId) {
        return gatheringRepository.findByGatheringId(gatheringId)
                .orElseThrow(() -> GatheringNotFoundException.EXCEPTION);
    }

    private void accessUser(User user) {
        if(!gatheringRepository.existsByUserId(user.getId())) {
            throw  UserAccessException.EXCEPTION;
        }
    }

    private Gathering getGathering(Long gatheringId){
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

    }


}
