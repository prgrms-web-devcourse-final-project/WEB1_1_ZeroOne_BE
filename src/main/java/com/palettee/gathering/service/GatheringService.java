package com.palettee.gathering.service;

import com.palettee.gathering.GatheringNotFoundException;
import com.palettee.gathering.controller.dto.Request.GatheringCommonRequest;
import com.palettee.gathering.controller.dto.Response.GatheringCommonResponse;
import com.palettee.gathering.controller.dto.Response.GatheringDetailsResponse;
import com.palettee.gathering.controller.dto.Response.GatheringLikeResponse;
import com.palettee.gathering.controller.dto.Response.GatheringResponse;
import com.palettee.gathering.domain.*;
import com.palettee.gathering.repository.GatheringRepository;
import com.palettee.global.s3.service.ImageService;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.service.NotificationService;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
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

    private final LikeRepository likeRepository;

    private final ImageService imageService;

    private final NotificationService notificationService;


    @Transactional
    public GatheringCommonResponse createGathering(GatheringCommonRequest request, User user) {

        User findByUser = getUser(user.getId());

        Gathering gathering = Gathering.builder()
                .user(findByUser)
                .period(request.period())
                .sort(Sort.findSort(request.sort()))
                .subject(Subject.finSubject(request.subject()))
                .contact(Contact.findContact(request.contact()))
                .deadLine(GatheringCommonRequest.getDeadLineLocalDate(request.deadLine()))
                .personnel(request.personnel())
                .gatheringImages(GatheringCommonRequest.getGatheringImage(request.gatheringImages()))
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


        Gathering gathering = gatheringRepository.findByFetchId(gatheringId).orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

        accessUser(user, gathering);

        gathering.updateGathering(request);

        if(request.gatheringImages()!= null) deleteImages(gathering);  // 업데이트시 이미지가 들어왓을시 본래 s3 이미지삭제

        return GatheringCommonResponse.toDTO(gathering);
    }



    @Transactional
    public GatheringCommonResponse deleteGathering(Long gatheringId, User user) {

        Gathering gathering = gatheringRepository.findByImageFetchId(gatheringId).orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

        accessUser(user, gathering);

        deleteImages(gathering);

        gatheringRepository.delete(gathering);

        return GatheringCommonResponse.toDTO(gathering);
    }
    @Transactional
    public GatheringCommonResponse updateStatusGathering(Long gatheringId, User user){

        Gathering gathering = getGathering(gatheringId);
        accessUser(user, gathering);

        gathering.updateStatusComplete();

        return GatheringCommonResponse.toDTO(gathering);
    }

    @Transactional
    public GatheringLikeResponse createGatheringLike(Long gatheringId, User user){

        User findUser = getUser(user.getId());
        Gathering gathering = getGathering(gatheringId);

        if(cancelLike(gatheringId, findUser)) {
            return GatheringLikeResponse.toDto(null);
        }

        Likes likes = Likes.builder()
                .likeType(LikeType.GATHERING)
                .user(findUser)
                .targetId(gatheringId)
                .build();

        Long targetId = gathering.getUser().getId();
        notificationService.send(NotificationRequest.like(targetId, user.getName()));

        return GatheringLikeResponse.toDto(likeRepository.save(likes));
    }

    @Transactional
    public void updateGatheringStatus(){
        gatheringRepository.updateStatusExpired();
    }

    public CustomSliceResponse findLikeList(
            Pageable pageable,
            Long userId,
            Long likeId
    ){
        return gatheringRepository.PageFindLikeGathering(pageable, userId, likeId);
    }

    public Gathering getGathering(Long gatheringId){
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> GatheringNotFoundException.EXCEPTION);

    }

    private Gathering getFetchGathering(Long gatheringId) {
        return gatheringRepository.findByGatheringId(gatheringId)
                .orElseThrow(() -> GatheringNotFoundException.EXCEPTION);
    }

    private void accessUser(User user, Gathering gathering) {
        if(gathering.getUser().getId() != user.getId()){
            throw  UserAccessException.EXCEPTION;
        }
    }


    private boolean cancelLike(Long gatheringId, User user) {
        Likes findByLikes = likeRepository.findByUserIdAndTargetId(user.getId(), gatheringId, LikeType.GATHERING);
        if(findByLikes != null) {
            likeRepository.delete(findByLikes);
            return true;
        }
        return false;
    }

    private User getUser(Long userId){
        return  userRepository.findById(userId).orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

    private void deleteImages(Gathering gathering) {
        if (!gathering.getGatheringImages().isEmpty()) {
            gathering.getGatheringImages().forEach(gatheringImage -> imageService.delete(gatheringImage.getImageUrl()));
        }
    }



}