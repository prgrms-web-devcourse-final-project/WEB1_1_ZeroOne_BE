package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;

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
import com.palettee.archive.repository.TagRepository;
import com.palettee.archive.service.ArchiveService;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ArchiveServiceTest {

    @Autowired ArchiveService archiveService;
    @Autowired UserRepository userRepository;
    @Autowired ArchiveRepository archiveRepository;
    @Autowired TagRepository tagRepository;
    @Autowired ArchiveImageRepository archiveImageRepository;

    private User savedUser;

    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(new User("email", "imageUrl","name", "briefIntro", MajorJobGroup.DEVELOPER, MinorJobGroup.BACKEND));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        archiveRepository.deleteAll();
        archiveImageRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 아카이브 등록 성공")
    void registerArchiveTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));

        //when
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());

        //then
        assertThat(archiveResponse.archiveId()).isNotNull();

        List<Tag> allTags = tagRepository.findAll();
        assertThat(allTags.size()).isEqualTo(2);
        assertThat(allTags.get(0).getContent()).isEqualTo("tag1");
        assertThat(allTags.get(1).getContent()).isEqualTo("tag2");

        List<ArchiveImage> allImages = archiveImageRepository.findAll();
        assertThat(allImages.size()).isEqualTo(2);
        assertThat(allImages.get(0).getImageUrl()).isEqualTo("url1");
        assertThat(allImages.get(1).getImageUrl()).isEqualTo("url2");

        Archive archive = archiveRepository.findById(archiveResponse.archiveId()).orElseThrow();
        assertThat(archive.getHits()).isEqualTo(0);
        assertThat(archive.getTitle()).isEqualTo(request.title());
        assertThat(archive.getDescription()).isEqualTo(request.description());
        assertThat(archive.getType()).isEqualTo(ArchiveType.RED);
    }

    @Test
    @DisplayName("정상적인 아카이브 전체 조회 성공")
    void getAllArchiveTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));

        //when
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse2 = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse3 = archiveService.registerArchive(request, savedUser.getEmail());

        ArchiveListResponse all = archiveService.getAllArchive("DEVELOPER", "BACKEND", "latest", PageRequest.of(0, 10));

        //then
        assertThat(all.archives().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("정상적인 나의 아카이브 전체 조회 성공")
    void getMyArchiveTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));

        //when
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse2 = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse3 = archiveService.registerArchive(request, savedUser.getEmail());

        ArchiveListResponse all = archiveService.getMyArchive(savedUser.getEmail());

        //then
        assertThat(all.archives().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("아카이브 검색 조회 성공")
    void searchArchiveTest() {
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));

        //when
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse2 = archiveService.registerArchive(request, savedUser.getEmail());
        ArchiveResponse archiveResponse3 = archiveService.registerArchive(request, savedUser.getEmail());

        ArchiveListResponse tag = archiveService.searchArchive("tag1", PageRequest.of(0, 10));
        ArchiveListResponse title = archiveService.searchArchive("title", PageRequest.of(0, 10));
        ArchiveListResponse description = archiveService.searchArchive("description", PageRequest.of(0, 10));

        //then
        assertThat(tag.archives().size()).isEqualTo(3);
        assertThat(title.archives().size()).isEqualTo(3);
        assertThat(description.archives().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("정상적인 아카이브 단건 조회 성공")
    void getArchiveDetailTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());

        //when
        ArchiveDetailResponse archiveDetail = archiveService.getArchiveDetail(archiveResponse.archiveId());

        //then
        assertThat(archiveResponse.archiveId()).isNotNull();

        List<TagDto> allTags = archiveDetail.tags();
        assertThat(allTags.size()).isEqualTo(2);
        assertThat(allTags.get(0).tag()).isEqualTo("tag1");
        assertThat(allTags.get(1).tag()).isEqualTo("tag2");

        List<ImageUrlDto> allImages = archiveDetail.imageUrls();
        assertThat(allImages.size()).isEqualTo(2);
        assertThat(allImages.get(0).url()).isEqualTo("url1");
        assertThat(allImages.get(1).url()).isEqualTo("url2");

        assertThat(archiveDetail.hits()).isEqualTo(1);
        assertThat(archiveDetail.title()).isEqualTo(request.title());
        assertThat(archiveDetail.description()).isEqualTo(request.description());
        assertThat(archiveDetail.type()).isEqualTo("RED");
        assertThat(archiveDetail.username()).isEqualTo(savedUser.getName());
        assertThat(archiveDetail.job()).isEqualTo(savedUser.getMinorJobGroup().name());
        assertThat(archiveDetail.likeCount()).isEqualTo(0L);
        assertThat(archiveDetail.commentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("정상적인 아카이브 수정 성공")
    void updateArchiveTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());

        //when
        ArchiveUpdateRequest archiveUpdateRequest = new ArchiveUpdateRequest("new _title", "new_description", "YELLOW",
                false,
                List.of(new TagDto("tag11"), new TagDto("tag12")),
                List.of(new ImageUrlDto("url11"), new ImageUrlDto("url12")));
        ArchiveResponse archiveResponse1 = archiveService.updateArchive(archiveResponse.archiveId(),
                archiveUpdateRequest);

        ArchiveDetailResponse archiveDetail = archiveService.getArchiveDetail(archiveResponse1.archiveId());

        //then
        assertThat(archiveResponse.archiveId()).isNotNull();

        List<TagDto> allTags = archiveDetail.tags();
        assertThat(allTags.size()).isEqualTo(2);
        assertThat(allTags.get(0).tag()).isEqualTo("tag11");
        assertThat(allTags.get(1).tag()).isEqualTo("tag12");

        List<ImageUrlDto> allImages = archiveDetail.imageUrls();
        assertThat(allImages.size()).isEqualTo(2);
        assertThat(allImages.get(0).url()).isEqualTo("url11");
        assertThat(allImages.get(1).url()).isEqualTo("url12");

        assertThat(archiveDetail.hits()).isEqualTo(1);
        assertThat(archiveDetail.title()).isEqualTo(archiveUpdateRequest.title());
        assertThat(archiveDetail.description()).isEqualTo(archiveUpdateRequest.description());
        assertThat(archiveDetail.type()).isEqualTo("YELLOW");
        assertThat(archiveDetail.username()).isEqualTo(savedUser.getName());
        assertThat(archiveDetail.job()).isEqualTo(savedUser.getMinorJobGroup().name());
        assertThat(archiveDetail.likeCount()).isEqualTo(0L);
        assertThat(archiveDetail.commentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("정상적인 아카이브 삭제 성공")
    void deleteArchiveTest() {
        // given
        ArchiveRegisterRequest request = new ArchiveRegisterRequest(
                "title", "description", "RED", true,
                List.of(new TagDto("tag1"), new TagDto("tag2")),
                List.of(new ImageUrlDto("url1"), new ImageUrlDto("url2")));
        ArchiveResponse archiveResponse = archiveService.registerArchive(request, savedUser.getEmail());

        //when
        ArchiveResponse archiveResponse1 = archiveService.deleteArchive(archiveResponse.archiveId());

        //then
        assertThat(archiveResponse1.archiveId()).isNotNull();

        List<Tag> allTags = tagRepository.findAll();
        assertThat(allTags.size()).isEqualTo(0);

        List<ArchiveImage> allImages = archiveImageRepository.findAll();
        assertThat(allImages.size()).isEqualTo(0);

    }

}
