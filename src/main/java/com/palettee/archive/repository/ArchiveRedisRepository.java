package com.palettee.archive.repository;

import com.palettee.archive.controller.dto.response.ArchiveRedisList;
import com.palettee.archive.controller.dto.response.ArchiveRedisResponse;
import com.palettee.archive.domain.Archive;
import io.jsonwebtoken.io.SerializationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ArchiveRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> redisTemplateForArchive;

    private final ArchiveRepository archiveRepository;
    private final ArchiveImageRepository archiveImageRepository;

    private static final String INCR_KEY_PREFIX = "incr:hit:archiveId:";
    private static final String STD_KEY_PREFIX = "std:hit:archiveId:";
    private static final String LIKE_KEY_PREFIX = "like:archiveId:";
    private static final String TOP_ARCHIVE_KEY = "top_archives";
    private static final String DELIMITER = ":";
    private static final long TOP_ARCHIVE_TTL = 1; // TTL in hours

    @Transactional
    public void settleHits() {
        Set<String> incrKeys = redisTemplate.keys(INCR_KEY_PREFIX + "*");
        if (incrKeys == null || incrKeys.isEmpty()) {
            return;
        }

        for (String incrKey : incrKeys) {
            String archiveId = extractId(incrKey);

            String incrHits = redisTemplate.opsForValue().get(incrKey);
            long incrCount = incrHits == null ? 0 : Long.parseLong(incrHits);

            String stdKey = STD_KEY_PREFIX + archiveId;
            String stdHits = redisTemplate.opsForValue().get(stdKey);
            long stdCount = stdHits == null ? 0 : Long.parseLong(stdHits);

            long totalHits = stdCount + incrCount;

            updateHitCount(Long.parseLong(archiveId), totalHits);

            redisTemplate.opsForValue().set(stdKey, String.valueOf(totalHits));
            redisTemplate.delete(incrKey);
        }
    }

    private void updateHitCount(Long archiveId, long totalHits) {
        Archive archive = archiveRepository.findById(archiveId).orElseThrow();
        archive.setHit(totalHits);
    }

    private String extractId(String key) {
        return key.split(DELIMITER)[3];
    }

    @Transactional(readOnly = true)
    public void updateArchiveList() {
        List<Long> top4ArchiveIds = getTop4ArchiveIds();
        List<Archive> archives = archiveRepository.findArchivesInIds(top4ArchiveIds);

        int redisSize = archives.size();
        if (redisSize < 4) {
            int remaining = 4 - redisSize;
            List<Archive> additionalFromDB = archiveRepository.findTopArchives(remaining);
            archives.addAll(additionalFromDB);
        }

        List<ArchiveRedisResponse> redisData = archives.stream()
                .map(archive -> ArchiveRedisResponse.toResponse(
                        archive, archiveImageRepository.getArchiveThumbnail(archive.getId())))
                .toList();

        redisTemplateForArchive.opsForValue().set(TOP_ARCHIVE_KEY, new ArchiveRedisList(redisData), TOP_ARCHIVE_TTL, TimeUnit.HOURS);
    }

    private List<Long> getTop4ArchiveIds() {
        Set<String> hitKeys = redisTemplate.keys(INCR_KEY_PREFIX + "*");
        Set<String> likeKeys = redisTemplate.keys(LIKE_KEY_PREFIX + "*");

        if (hitKeys == null || likeKeys == null) {
            return Collections.emptyList();
        }

        Map<Long, Integer> archiveScores = new HashMap<>();
        for (String hitKey : hitKeys) {
            Long archiveId = extractArchiveId(hitKey);
            int hitCount = getValueAsInt(hitKey);

            String likeKey = LIKE_KEY_PREFIX + archiveId;
            int likeCount = getValueAsInt(likeKey);

            int score = hitCount + (likeCount * 5);
            archiveScores.put(archiveId, score);
        }

        return archiveScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Long extractArchiveId(String key) {
        return Long.valueOf(key.replace(INCR_KEY_PREFIX, ""));
    }

    private int getValueAsInt(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    public ArchiveRedisList getTopArchives() {
        try {
            ArchiveRedisList result = (ArchiveRedisList) redisTemplateForArchive.opsForValue().get(TOP_ARCHIVE_KEY);
            return result == null ? new ArchiveRedisList(new ArrayList<>()) : result;
        } catch (SerializationException e) {
            redisTemplateForArchive.delete(TOP_ARCHIVE_KEY);
            return new ArchiveRedisList(new ArrayList<>());
        }
    }
}
