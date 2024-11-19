package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ArchiveUpdateRequest;
import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.controller.dto.response.ArchiveDetailResponse;
import com.palettee.archive.controller.dto.response.ArchiveListResponse;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

        processingTags(archiveRegisterRequest.tags(), archive);
        processingImage(archiveRegisterRequest.imageUrls(), archive);

        return new ArchiveResponse(savedArchive.getId());
    }

//    public ArchiveListResponse getAllArchive(String sortCondition, String category, String email) {
//
//        User user = getUser(email);
//
//        PageRequest recentCondition = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//        PageRequest recommendCondition = PageRequest.of(0, 10, Sort.by("hits").descending());
//
//        if (category.equals("all")) {
//            Page<Archive> all = archiveRepository.findAll(recentCondition);
//        } else {
//
//        }
//    }

    public ArchiveDetailResponse getArchiveDetail(Long archiveId) {
        Archive archive = getArchive(archiveId);
        List<ImageUrlDto> urlDtoList = archiveImageRepository.findByArchiveId(archiveId)
                .stream().map(ImageUrlDto::new).toList();
        List<TagDto> tagDtoList = tagRepository.findByArchiveId(archiveId)
                .stream().map(TagDto::new).toList();
        long count = commentRepository.countArchiveComment(archiveId);
        long likeCount = likeRepository.countArchiveLike(archiveId);
        return new ArchiveDetailResponse(
                archive.getTitle(),
                archive.getDescription(),
                archive.getUser().getName(),
                archive.getType().name(),
                archive.isCanComment(),
                archive.getUser().getEmail(), //수정 필요
                likeCount,
                count,
                archive.getHits(),
                tagDtoList,
                urlDtoList
        );
    }

    @Transactional
    public ArchiveResponse updateArchive(Long archiveId, ArchiveUpdateRequest archiveUpdateRequest) {
        Archive archive = getArchive(archiveId);
        Archive updatedArchive = archive.update(archiveUpdateRequest);
        return new ArchiveResponse(updatedArchive.getId());
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
