package com.palettee.gathering.controller;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.controller.dto.Response.GatheringLikeResponse;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.security.validation.UserUtils;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/gathering")
public class GatheringController {

    private final GatheringService gatheringService;


    @PostMapping()
    public GatheringCommonResponse create(@RequestBody @Valid GatheringCommonRequest request) {

        GatheringCommonResponse gathering = gatheringService.createGathering(request, UserUtils.getContextUser());

        return gathering;
    }

    @GetMapping()
    public CustomSliceResponse findAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long gatheringId,
            Pageable pageable
    ) {
        return gatheringService.findAll(sort, subject, period, position, status, gatheringId, pageable);
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
        GatheringCommonResponse gatheringCommonResponse = gatheringService.updateGathering(gatheringId, request, UserUtils.getContextUser());

        return gatheringCommonResponse;
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

    @GetMapping("/my-page")
    public CustomSliceResponse findLike(
            Pageable pageable ,
            @RequestParam(required = false) Long likeId
    ){
        User contextUser = UserUtils.getContextUser();

        return gatheringService.findLikeList(pageable, contextUser.getId(), likeId);
    }

}
