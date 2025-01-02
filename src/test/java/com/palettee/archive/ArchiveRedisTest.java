package com.palettee.archive;

import static org.assertj.core.api.Assertions.*;

import com.palettee.archive.controller.dto.response.ArchiveRedisResponse;
import com.palettee.archive.domain.Archive;
import com.palettee.archive.domain.ArchiveType;
import com.palettee.archive.event.HitEvent;
import com.palettee.archive.repository.ArchiveRedisRepository;
import com.palettee.archive.repository.ArchiveRepository;
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
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplateForArchive;

    private User savedUser;

    @BeforeEach
    void beforeEach() {
        savedUser = userRepository.save(
                User.builder()
                        .email("email").imageUrl("imageUrl").name("name").briefIntro("briefIntro")
                        .userRole(UserRole.REAL_NEWBIE)
                        .majorJobGroup(MajorJobGroup.DEVELOPER)
                        .minorJobGroup(MinorJobGroup.BACKEND)
                        .build()
        );
    }

    @AfterEach
    public void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        redisTemplateForArchive.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("Redis hit count 정산 및 DB 업데이트 테스트")
    public void testSettleHits() {
        // given
        String incrKey = "incr:hit:archiveId:1";
        redisTemplate.opsForValue().set(incrKey, "5");

        Archive archive = archiveRepository.save(Archive.builder()
                .title("Test Archive")
                .description("Test Description")
                .introduction("Test Introduction")
                .type(ArchiveType.RED)
                .canComment(true)
                .user(savedUser)
                .build());

        // when
        archiveRedisRepository.settleHits();

        // then
        Archive updatedArchive = archiveRepository.findById(archive.getId()).orElseThrow();
        assertThat(updatedArchive.getHits()).isEqualTo(5L); // 0 + 5
        assertThat(redisTemplate.opsForValue().get(incrKey)).isNull();
    }

    @Test
    @DisplayName("인기 아카이브 업데이트 테스트")
    public void testUpdateMainArchive() {
        // given

        Archive archive1 = archiveRepository.save(Archive.builder()
                .title("Archive 1")
                .description("Description 1")
                .introduction("Introduction 1")
                .type(ArchiveType.RED)
                .canComment(true)
                .user(savedUser)
                .build());

        Archive archive2 = archiveRepository.save(Archive.builder()
                .title("Archive 2")
                .description("Description 2")
                .introduction("Introduction 2")
                .type(ArchiveType.RED)
                .canComment(false)
                .user(savedUser)
                .build());

        // when
        archiveRedisRepository.updateArchiveList();

        // then
        @SuppressWarnings("unchecked")
        List<Archive> topArchives = (List<Archive>) redisTemplateForArchive.opsForValue().get("top_archives");
        assertThat(topArchives).isNotNull();
        assertThat(topArchives).hasSize(2);
        assertThat(topArchives.get(0).getTitle()).isEqualTo("Archive 1");
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

    @Test
    @DisplayName("인기 아카이브 조회 테스트")
    public void testGetTopArchives() {
        // given

        Archive archive = archiveRepository.save(Archive.builder()
                .title("Top Archive")
                .description("Top Description")
                .introduction("Top Introduction")
                .type(ArchiveType.RED)
                .canComment(true)
                .user(savedUser)
                .build());

        redisTemplateForArchive.opsForValue().set("top_archives", List.of(archive));

        // when
        List<ArchiveRedisResponse> topArchives = archiveRedisRepository.getTopArchives();

        // then
        assertThat(topArchives).isNotNull();
        assertThat(topArchives).hasSize(1);
        assertThat(topArchives.get(0).title()).isEqualTo("Top Archive");
    }

}
