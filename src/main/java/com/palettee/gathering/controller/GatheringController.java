package com.palettee.gathering.controller;

import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.repository.GatheringRedisRepository;
import com.palettee.gathering.controller.dto.Response.GatheringPopularResponse;
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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/gathering")
public class GatheringController {

    private final GatheringService gatheringService;

    private final GatheringRedisRepository redisRepository;


    @PostMapping()
    public GatheringCommonResponse create(@RequestBody @Valid GatheringCommonRequest request) {

        GatheringCommonResponse gathering = gatheringService.createGathering(request, UserUtils.getContextUser());
        redisRepository.addGatheringInRedis(gathering.gatheringId());

        return gathering;
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
        return gatheringService.findAll(sort, subject, period, contact, positions, status, personnel, gatheringId, pageable,getUserFromContext() ,isFirstTrue(gatheringId, sort, subject, period, contact, status, positions, personnel));
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

        redisRepository.updateGatheringInRedis(gatheringCommonResponse.gatheringId());

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

    @GetMapping("/main")
    public List<GatheringPopularResponse> findPopularGathering(){
        return gatheringService.gatheringPopular(getUserFromContext());
    }

    private Optional<User> getUserFromContext() {
        User user = null;
        try {
            user = UserUtils.getContextUser();
        } catch (Exception e) {
            log.info("Current user is not logged in");
        }

        return Optional.ofNullable(user);
    }

    private static boolean isFirstTrue(Long gatheringId, String sort, String subject, String period, String contact,String status ,List<String> positions, int personnel) {
        if(gatheringId != null || sort != null || subject != null || period != null || contact != null || !status.equals("모집중") || !positions.isEmpty() || personnel > 0){
            return false;
        }
        return true;
    }

}
