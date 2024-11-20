package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ArchiveUpdateRequest;
import com.palettee.archive.controller.dto.request.ChangeOrderRequest;
import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.controller.dto.response.ArchiveDetailResponse;
import com.palettee.archive.controller.dto.response.ArchiveListResponse;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
import com.palettee.archive.controller.dto.response.ArchiveSimpleResponse;
import com.palettee.archive.controller.dto.response.SliceInfo;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.ArchiveImage;
import com.palettee.archive.domain.ArchiveType;
import com.palettee.archive.domain.Tag;
import com.palettee.archive.repository.ArchiveImageRepository;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.CommentRepository;
import com.palettee.archive.repository.TagRepository;
import com.palettee.likes.repository.LikeRepository;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ArchiveImageRepository archiveImageRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public ArchiveResponse registerArchive(ArchiveRegisterRequest archiveRegisterRequest, String email) {
        User user = getUser(email);
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

        return new ArchiveResponse(savedArchive.getId());
    }

    public ArchiveListResponse getAllArchive(String category, Pageable pageable) {
        Slice<Archive> archives = archiveRepository.findAllArchiveWithCondition(category, pageable);

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

    public ArchiveListResponse getMyArchive(String email) {
        User user = getUser(email);
        List<ArchiveSimpleResponse> result = archiveRepository.getAllMyArchive(user.getUserId())
                .stream()
                .map(it -> ArchiveSimpleResponse.toResponse(it, likeRepository))
                .toList();
        return new ArchiveListResponse(result, null);
    }

    public ArchiveListResponse getLikeArchive(String email) {
        User user = getUser(email);
        List<Long> ids = likeRepository.findMyLikeList(user.getUserId());

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
        archiveRepository.deleteById(archiveId);
        tagRepository.deleteAllByArchiveId(archiveId);
        archiveImageRepository.deleteAllByArchiveId(archiveId);
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
        return archiveRepository.findById(archiveId).orElseThrow(() -> new IllegalArgumentException(""));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }

}
