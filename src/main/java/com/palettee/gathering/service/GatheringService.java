package com.palettee.gathering.service;

import com.palettee.gathering.controller.dto.Request.GatheringCreateRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCreateResponse;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
