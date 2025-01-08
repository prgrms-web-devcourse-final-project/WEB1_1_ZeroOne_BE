package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.palettee.archive.controller.dto.response.ArchiveRedisList;
import com.palettee.archive.controller.dto.response.ArchiveRedisResponse;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.ArchiveType;
import com.palettee.archive.event.HitEvent;
import com.palettee.archive.repository.ArchiveRedisRepository;
import com.palettee.archive.repository.ArchiveRepository;
import com.palettee.archive.service.ArchiveScheduler;
import com.palettee.archive.service.ArchiveService;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import com.palettee.user.domain.User;
import com.palettee.user.domain.UserRole;
import com.palettee.user.repository.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ArchiveRedisTest {

    @Autowired
    private ArchiveRedisRepository archiveRedisRepository;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplateForArchive;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(
                User.builder()
                        .email("email").imageUrl("imageUrl").name("name").briefIntro("briefIntro")
                        .userRole(UserRole.USER)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {

        archiveRepository.deleteAll();
        userRepository.deleteAll();

        redisTemplate.keys("*").forEach(redisTemplate::delete);
        redisTemplateForArchive.keys("*").forEach(redisTemplateForArchive::delete);
    }

    @Test
    void testSettleHits() {
        // Given

        Archive archive = new Archive("title", "description", "introduction", ArchiveType.RED, true, savedUser);
        archiveRepository.save(archive);

        String incrKey = "incr:hit:archiveId:" + archive.getId();
        String stdKey = "std:hit:archiveId:" + archive.getId();

        redisTemplate.opsForValue().set(incrKey, "5");
        redisTemplate.opsForValue().set(stdKey, "10");

        // When
        archiveRedisRepository.settleHits();

        // Then
        assertThat(redisTemplate.opsForValue().get(stdKey)).isEqualTo("15");
        assertThat(redisTemplate.hasKey(incrKey)).isFalse();

        Archive updatedArchive = archiveRepository.findById(archive.getId()).orElseThrow();
        assertThat(updatedArchive.getHits()).isEqualTo(15);
    }

    @Test
    void testRedisSerialization() {
        ArchiveRedisResponse archive = new ArchiveRedisResponse(1L, "Test", "Description", "Intro", "User", "Type", true, 10L, false, "url", "2025-01-04");
        redisTemplateForArchive.opsForValue().set("test_archive", archive);

        ArchiveRedisResponse result = (ArchiveRedisResponse) redisTemplateForArchive.opsForValue().get("test_archive");
        assertNotNull(result);
        assertEquals(archive.archiveId(), result.archiveId());
    }

    @Test
    void testUpdateArchiveList() {
        // Given
        Archive archive1 = new Archive("Archive 1", "description", "introduction", ArchiveType.RED, true, savedUser);
        Archive archive2 = new Archive("Archive 2", "description", "introduction", ArchiveType.RED, true, savedUser);
        Archive archive3 = new Archive("Archive 2", "description", "introduction", ArchiveType.RED, true, savedUser);
        Archive archive4 = new Archive("Archive 2", "description", "introduction", ArchiveType.RED, true, savedUser);
        archiveRepository.saveAll(List.of(archive1, archive2, archive3, archive4));

        String incrKey1 = "incr:hit:archiveId:" + archive1.getId();
        String incrKey2 = "incr:hit:archiveId:" + archive2.getId();

        redisTemplate.opsForValue().set(incrKey1, "30");
        redisTemplate.opsForValue().set(incrKey2, "40");

        // When
        archiveRedisRepository.updateArchiveList();

        // Then
        ArchiveRedisList topArchives = archiveRedisRepository.getTopArchives();
        assertThat(topArchives.archives()).hasSize(4);
        assertThat(topArchives.archives())
                .extracting(ArchiveRedisResponse::archiveId)
                .containsExactlyInAnyOrder(archive2.getId(), archive1.getId(), archive4.getId(), archive3.getId());
    }

    @Test
    @DisplayName("Hit 이벤트 처리 테스트")
    public void testOnHitEvent() {
        // given
        String email = "test@example.com";
        Long archiveId = 1L;

        HitEvent hitEvent = new HitEvent(archiveId, email);
        String setKey = "hit:archiveId:" + archiveId;
        String valueKey = "incr:hit:archiveId:" + archiveId;

        // when
        redisTemplate.opsForSet().add(setKey, email);
        redisTemplate.opsForValue().increment(valueKey);

        // then
        Long hitCount = redisTemplate.opsForValue().getOperations().opsForValue().increment(valueKey, 0);
        assertThat(hitCount).isEqualTo(1L);

        Set<String> setMembers = redisTemplate.opsForSet().members(setKey);
        assertThat(setMembers).contains(email);
    }

}
