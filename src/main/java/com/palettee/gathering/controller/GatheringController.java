package com.palettee.gathering.controller;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.controller.dto.Response.GatheringLikeResponse;
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
    public GatheringCommonResponse create(@RequestBody @Valid GatheringCommonRequest request) {
        return gatheringService.createGathering(request, UserUtils.getContextUser());
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

    @GetMapping("/{gatheringId}")
    public GatheringDetailsResponse findByGatheringId(@PathVariable Long gatheringId) {
        return gatheringService.findByDetails(gatheringId);
    }

    @PutMapping("/{gatheringId}")
    public GatheringCommonResponse update(
            @RequestBody @Valid GatheringCommonRequest request,
            @PathVariable Long gatheringId
    ) {
        return gatheringService.updateGathering(gatheringId, request, UserUtils.getContextUser());
    }

    @DeleteMapping("/{gatheringId}")
    public GatheringCommonResponse delete(@PathVariable Long gatheringId){
        return gatheringService.deleteGathering(gatheringId, UserUtils.getContextUser());
    }

    @PatchMapping("/{gatheringId}")
    public GatheringCommonResponse updateStatus(@PathVariable Long gatheringId){
        return gatheringService.updateStatusGathering(gatheringId, UserUtils.getContextUser());
    }

    @PostMapping("/{gatheringId}/like")
    public GatheringLikeResponse createLike(@PathVariable Long gatheringId){
        return gatheringService.createGatheringLike(gatheringId, UserUtils.getContextUser());
    }
}
