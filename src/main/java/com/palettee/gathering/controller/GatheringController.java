package com.palettee.gathering.controller;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.service.GatheringService;
import com.palettee.global.security.validation.UserUtils;
import com.palettee.gathering.controller.dto.Response.CustomSliceResponse;
import com.palettee.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public CustomSliceResponse findAll(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false, defaultValue = "") List<String> positions,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long gatheringId,
            @RequestParam(required = false, defaultValue = "0") int personnel,
            Pageable pageable
    ) {
        return gatheringService.findAll(sort, subject, period, contact, positions, status, personnel, gatheringId, pageable, isFirstTrue(gatheringId));
    }

    private static boolean isFirstTrue(Long gatheringId) {
        if(gatheringId != null){
            return false;
        }
        return true;
    }

    @GetMapping("/{gatheringId}")
    public GatheringDetailsResponse findByGatheringId(@PathVariable Long gatheringId) {
        return gatheringService.findByDetails(gatheringId, UserUtils.getContextUser().getId());
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
    public boolean createLike(@PathVariable Long gatheringId){
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
