package com.palettee.archive.service;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.ArchiveImage;
import com.palettee.archive.domain.ArchiveType;
import com.palettee.archive.domain.Tag;
import com.palettee.archive.repository.ArchiveImageRepository;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.TagRepository;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }

}
