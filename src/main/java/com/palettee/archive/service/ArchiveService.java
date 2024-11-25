package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.*;
import com.palettee.archive.controller.dto.response.*;
import com.palettee.archive.domain.*;
import com.palettee.archive.exception.*;
import com.palettee.archive.repository.*;
import com.palettee.likes.repository.*;
import com.palettee.user.domain.*;
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

    @Transactional
    public ArchiveResponse registerArchive(ArchiveRegisterRequest archiveRegisterRequest, User user) {
        Archive archive = Archive.builder()
                .title(archiveRegisterRequest.title())
                .description(archiveRegisterRequest.description())
                .type(ArchiveType.findByInput(archiveRegisterRequest.colorType()))
                .canComment(archiveRegisterRequest.canComment())
                .user(user)
                .build();

        Archive savedArchive = archiveRepository.save(archive);
        archive.setOrder();
        processingTags(archiveRegisterRequest.tags(), archive);
        processingImage(archiveRegisterRequest.imageUrls(), archive);

        // 아카이드 등록시 유저 권한 상승
        user.changeUserRole(UserRole.USER);

        return new ArchiveResponse(savedArchive.getId());
    }

    public ArchiveListResponse getAllArchive(String majorJobGroup, String minorJobGroup, String sort, Pageable pageable) {
        Slice<Archive> archives = archiveRepository.findAllArchiveWithCondition(majorJobGroup, minorJobGroup, sort, pageable);

        List<ArchiveSimpleResponse> list = archives
                .map(it -> ArchiveSimpleResponse.toResponse(it, likeRepository))
                .toList();

        return new ArchiveListResponse(list, SliceInfo.of(archives));
    }

    @Transactional
    public ArchiveDetailResponse getArchiveDetail(Long archiveId) {
        Archive archive = getArchive(archiveId);
        archive.hit();
        return ArchiveDetailResponse.toResponse(
                archive,
                likeRepository.countArchiveLike(archiveId),
                commentRepository.countArchiveComment(archiveId),
                tagRepository.findByArchiveId(archiveId)
                        .stream().map(TagDto::new).toList(),
                archiveImageRepository.findByArchiveId(archiveId)
                        .stream().map(ImageUrlDto::new).toList()
        );
    }

    public ArchiveListResponse getMyArchive(User user) {
        List<ArchiveSimpleResponse> result = archiveRepository.getAllMyArchive(user.getId())
                .stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, likeRepository))
                .toList();
        return new ArchiveListResponse(result, null);
    }

    public ArchiveListResponse getLikeArchive(User user) {
        List<Long> ids = likeRepository.findMyLikeList(user.getId());

        List<ArchiveSimpleResponse> result = archiveRepository.findAllInIds(ids)
                .stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, likeRepository))
                .toList();
        return new ArchiveListResponse(result, null);
    }

    public ArchiveListResponse searchArchive(String searchKeyword, Pageable pageable) {
        List<Long> ids = tagRepository.findAllArchiveIds(searchKeyword);
        Slice<Archive> archives = archiveRepository.searchArchive(searchKeyword, ids, pageable);

        List<ArchiveSimpleResponse> list = archives
                .map(it -> ArchiveSimpleResponse.toResponse(it, likeRepository))
                .toList();

        return new ArchiveListResponse(list, SliceInfo.of(archives));
    }

    @Transactional
    public ArchiveResponse updateArchive(Long archiveId, ArchiveUpdateRequest archiveUpdateRequest) {
        Archive archive = getArchive(archiveId);
        Archive updatedArchive = archive.update(archiveUpdateRequest);

        deleteAllInfo(archiveId);
        processingTags(archiveUpdateRequest.tags(), archive);
        processingImage(archiveUpdateRequest.imageUrls(), archive);

        return new ArchiveResponse(updatedArchive.getId());
    }

    @Transactional
    public ArchiveResponse deleteArchive(Long archiveId) {
        tagRepository.deleteAllByArchiveId(archiveId);
        archiveImageRepository.deleteAllByArchiveId(archiveId);
        Archive archive = getArchive(archiveId);
        archiveRepository.delete(archive);
        return new ArchiveResponse(archiveId);
    }

    private void deleteAllInfo(Long archiveId) {
        tagRepository.deleteAllByArchiveId(archiveId);
        archiveImageRepository.deleteAllByArchiveId(archiveId);
    }

    @Transactional
    public void changeArchiveOrder(ChangeOrderRequest changeOrderRequest) {
        Map<Long, Integer> map = changeOrderRequest.orderRequest();

        for (Long pk : map.keySet()) {
            Archive archive = getArchive(pk);
            archive.updateOrder(map.get(pk));
        }
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

}
