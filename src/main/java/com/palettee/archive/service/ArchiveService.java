package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.*;
import com.palettee.archive.controller.dto.response.*;
import com.palettee.archive.domain.*;
import com.palettee.archive.exception.*;
import com.palettee.archive.repository.*;
import com.palettee.likes.domain.LikeType;
import com.palettee.likes.domain.Likes;
import com.palettee.likes.repository.*;
import com.palettee.notification.controller.dto.NotificationRequest;
import com.palettee.notification.service.NotificationService;
import com.palettee.user.domain.*;
import com.palettee.user.exception.UserNotFoundException;
import com.palettee.user.repository.UserRepository;
import java.util.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final TagRepository tagRepository;
    private final ArchiveImageRepository archiveImageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ArchiveResponse registerArchive(ArchiveRegisterRequest archiveRegisterRequest, User user) {
        User findUser = getUser(user.getId());
        Archive archive = Archive.builder()
                .title(archiveRegisterRequest.title())
                .description(archiveRegisterRequest.description())
                .introduction(archiveRegisterRequest.introduction())
                .type(ArchiveType.findByInput(archiveRegisterRequest.colorType()))
                .canComment(archiveRegisterRequest.canComment())
                .user(findUser)
                .build();

        Archive savedArchive = archiveRepository.save(archive);
        archive.setOrder();
        processingTags(archiveRegisterRequest.tags(), archive);
        processingImage(archiveRegisterRequest.imageUrls(), archive);

        // 아카이브 등록시 유저 권한 상승
        findUser.changeUserRole(UserRole.USER);

        return new ArchiveResponse(savedArchive.getId());
    }

    public ArchiveListResponse getAllArchive(String color, String sort, Pageable pageable, User optionalUser) {
        Slice<Archive> archives = archiveRepository.findAllArchiveWithCondition(color, sort, pageable);
        List<ColorCount> colorCounts = archiveRepository.countByArchiveType();
        Long myId = getOptionalUserId(optionalUser);

        List<ArchiveSimpleResponse> list = archives
                .map(it -> ArchiveSimpleResponse.toResponse(it, myId, likeRepository))
                .toList();

        return new ArchiveListResponse(list, colorCounts, SliceInfo.of(archives));
    }

    private Long getOptionalUserId(User optionalUser) {
        return optionalUser == null ? 0L : optionalUser.getId();
    }

    public ArchiveListResponse getMainArchive(User optionalUser) {
        Long myId = getOptionalUserId(optionalUser);
        List<ArchiveSimpleResponse> result = archiveRepository.getMainArchives().stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, myId, likeRepository))
                .toList();
        return new ArchiveListResponse(result,null, null);
    }

    @Transactional
    public ArchiveDetailResponse getArchiveDetail(Long archiveId, User user) {
        Archive archive = getArchive(archiveId);
        archive.hit();
        Long userId = user == null ? 0L : user.getId();
        return ArchiveDetailResponse.toResponse(
                archive,
                userId,
                likeRepository.countArchiveLike(archiveId),
                commentRepository.countArchiveComment(archiveId),
                likeRepository.existByUserAndArchive(archiveId, userId).isPresent(),
                tagRepository.findByArchiveId(archiveId)
                        .stream().map(TagDto::new).toList(),
                archiveImageRepository.findByArchiveId(archiveId)
                        .stream().map(ImageUrlDto::new).toList()
        );
    }

    public ArchiveListResponse getMyArchive(User user, Pageable pageable) {

        Slice<Archive> archives = archiveRepository.getAllMyArchive(user.getId(), pageable);

        List<ArchiveSimpleResponse> result = archives
                .stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, user.getId(), likeRepository))
                .toList();
        List<ColorCount> colorCounts = archiveRepository.countMyArchiveByArchiveType(user.getId());
        return new ArchiveListResponse(result, colorCounts, SliceInfo.of(archives));
    }

    public ArchiveListResponse getLikeArchive(User user, Pageable pageable) {
        List<Long> ids = likeRepository.findMyLikeList(user.getId());
        List<ColorCount> colorCounts = archiveRepository.countLikeArchiveByArchiveType(ids);
        Slice<Archive> archives = archiveRepository.findAllInIds(ids, pageable);
        List<ArchiveSimpleResponse> result = archives
                .stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, user.getId(), likeRepository))
                .toList();
        return new ArchiveListResponse(result, colorCounts, SliceInfo.of(archives));
    }

    public ArchiveListResponse searchArchive(String searchKeyword, Pageable pageable, User optionalUser) {
        List<Long> ids = tagRepository.findAllArchiveIds(searchKeyword);
        Long myId = getOptionalUserId(optionalUser);
        Slice<Archive> archives = archiveRepository.searchArchive(searchKeyword, ids, pageable);

//        List<Long> archiveIds = archives.stream()
//                .map(Archive::getId)
//                .toList();
//        List<ColorCount> colorCounts = archiveRepository.countLikeArchiveByArchiveType(archiveIds);

        List<ArchiveSimpleResponse> list = archives
                .map(it -> ArchiveSimpleResponse.toResponse(it, myId, likeRepository))
                .toList();

        return new ArchiveListResponse(list, null, SliceInfo.of(archives));
    }

    @Transactional
    public ArchiveResponse updateArchive(Long archiveId, ArchiveUpdateRequest archiveUpdateRequest, User user) {
        Archive archive = getArchive(archiveId);
        checkArchiveOwner(user, archive);

        Archive updatedArchive = archive.update(archiveUpdateRequest);

        deleteAllInfo(archiveId);
        processingTags(archiveUpdateRequest.tags(), archive);
        processingImage(archiveUpdateRequest.imageUrls(), archive);

        return new ArchiveResponse(updatedArchive.getId());
    }

    @Transactional
    public ArchiveResponse deleteArchive(Long archiveId, User user) {
        Archive archive = getArchive(archiveId);

        checkArchiveOwner(user, archive);

        tagRepository.deleteByArchive(archive);
        archiveImageRepository.deleteByArchive(archive);
        archiveRepository.delete(archive);
        return new ArchiveResponse(archiveId);
    }

    private void deleteAllInfo(Long archiveId) {
        tagRepository.deleteAllByArchiveId(archiveId);
        archiveImageRepository.deleteAllByArchiveId(archiveId);
    }

    @Transactional
    public void changeArchiveOrder(ChangeOrderRequest changeOrderRequest, User user) {
        Map<Long, Integer> map = changeOrderRequest.orderRequest();

        for (Long pk : map.keySet()) {
            Archive archive = getArchive(pk);
            checkArchiveOwner(user, archive);
            archive.updateOrder(map.get(pk));
        }
    }

    private void checkArchiveOwner(User user, Archive archive) {
        if (!archive.getUser().getId().equals(user.getId())) {
            throw NotArchiveOwner.EXCEPTION;
        }
    }

    @Transactional
    public ArchiveResponse likeArchive(Long archiveId, User user) {
        Likes findLike = likeRepository.findByUserIdAndTargetId(user.getId(), archiveId, LikeType.ARCHIVE);
        Archive archive = getArchive(archiveId);
        if(findLike != null) {
            likeRepository.delete(findLike);
            return new ArchiveResponse(archive.getId());
        }

        User findUser = getUser(user.getId());
        Likes like = Likes.builder()
                .likeType(LikeType.ARCHIVE)
                .targetId(archiveId)
                .user(findUser)
                .build();
        likeRepository.save(like);

        Long targetId = archive.getUser().getId();
        notificationService.send(NotificationRequest.like(targetId, user.getName()));

        return new ArchiveResponse(archive.getId());
    }

    private void processingTags(List<TagDto> tags, Archive archive) {
        for (TagDto dto : tags) {
            tagRepository.save(new Tag(dto.tag(), archive));
        }
    }

    private void processingImage(List<ImageUrlDto> imageUrls, Archive archive) {
        for (ImageUrlDto dto : imageUrls) {
            archiveImageRepository.save(new ArchiveImage(dto.url(), archive));
        }
    }

    private Archive getArchive(Long archiveId) {
        return archiveRepository.findById(archiveId).orElseThrow(() -> ArchiveNotFound.EXCEPTION);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }

}
