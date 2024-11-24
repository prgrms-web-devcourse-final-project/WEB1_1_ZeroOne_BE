package com.palettee.gathering.controller;

import com.palettee.gathering.controller.dto.Request.GatheringCreateRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCreateResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.security.validation.UserUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/gathering")
public class GatheringController {

    private final GatheringService gatheringService;

    @PostMapping()
    public GatheringCreateResponse create(@RequestBody @Valid GatheringCreateRequest request) {

        return gatheringService.createGathering(request,  UserUtils.getContextUser());
    }

    @GetMapping()
    public Slice<GatheringResponse> findAll(
            @RequestParam String sort,
            @RequestParam String period,
            @RequestParam String position,
            @RequestParam String status,
            @RequestParam(required = false) Long gatheringId,
            Pageable pageable
    ) {
        return gatheringService.findAll(sort, period, position, status, gatheringId, pageable);
    }

}
