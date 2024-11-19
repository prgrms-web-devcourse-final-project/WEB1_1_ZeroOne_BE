package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.controller.dto.request.ArchiveRegisterRequest;
import com.palettee.archive.controller.dto.request.ImageUrlDto;
import com.palettee.archive.controller.dto.request.TagDto;
import com.palettee.archive.controller.dto.response.ArchiveResponse;
import com.palettee.archive.domain.ArchiveImage;
import com.palettee.archive.domain.Tag;
import com.palettee.archive.repository.ArchiveImageRepository;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.repository.TagRepository;
import com.palettee.archive.service.ArchiveService;
import com.palettee.user.domain.User;
import com.palettee.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        savedUser = userRepository.save(new User("email", "imageUrl","name", "briefIntro"));
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
    }

}
