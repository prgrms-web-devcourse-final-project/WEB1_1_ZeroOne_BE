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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ArchiveRedisRepository {

    private static final String INCR_PATTERN = "incr:hit:archiveId:*";
    private static final String STD_PATTERN = "std:hit:archiveId:";
    private static final String TOP_ARCHIVE = "top_archives";
    private static final String DELIMITER = ":";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> redisTemplateForArchive;

    private final ArchiveRepository archiveRepository;

    @Transactional
    public void settleHits() {
        Set<String> incrKeys = redisTemplate.keys(INCR_PATTERN);
        if (incrKeys == null || incrKeys.isEmpty()) {
            return;
        }

        for (String incrKey : incrKeys) {
            String archiveId = extractId(incrKey);

            String incrHits = redisTemplate.opsForValue().get(incrKey);
            long incrCount = incrHits == null ? 0 : Long.parseLong(incrHits);

            String stdKey = STD_PATTERN + archiveId;
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

    private String extractId(String incrKey) {
        return incrKey.split(DELIMITER)[3];
    }

    @Transactional(readOnly = true)
    public void updateArchiveList() {
        List<Long> top4IncrKeys = getTop4IncrKeys();
        List<Archive> result = archiveRepository.findArchivesInIds(top4IncrKeys);

        int redisSize = result.size();
        if (redisSize < 4) {
            int remaining = 4 - redisSize;
            List<Archive> additionalFromDB = archiveRepository.findTopArchives(remaining);
            result.addAll(additionalFromDB);
        }

        List<ArchiveRedisResponse> redis = result.stream()
                        .map(ArchiveRedisResponse::toResponse)
                        .toList();
        log.info("Redis에 저장될 데이터: {}", redis);
        redisTemplateForArchive.opsForValue().set(TOP_ARCHIVE, new ArchiveRedisList(redis), 1, TimeUnit.HOURS);
    }

    private List<Long> getTop4IncrKeys() {
        String hitPattern = "incr:hit:archiveId:*";
        String likePattern = "like:archiveId:*";

        // Redis에서 키 가져오기
        Set<String> hitKeys = redisTemplate.keys(hitPattern);
        Set<String> likeKeys = redisTemplate.keys(likePattern);

        if (hitKeys == null || likeKeys == null) {
            return Collections.emptyList();
        }

        // 점수 계산
        Map<Long, Integer> archiveScores = new HashMap<>();
        for (String hitKey : hitKeys) {
            Long archiveId = extractArchiveId(hitKey);
            Integer hitCount = getValueAsInt(hitKey);

            String likeKey = "like:archiveId:" + archiveId;
            Integer likeCount = getValueAsInt(likeKey);

            int score = hitCount + (likeCount * 5);
            archiveScores.put(archiveId, score);
        }

        // 정렬 및 상위 4개 추출
        return archiveScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Long extractArchiveId(String key) {
        return Long.valueOf(key.replace("incr:hit:archiveId:", ""));
    }

    private Integer getValueAsInt(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    public ArchiveRedisList getTopArchives() {
        try {
            ArchiveRedisList result = (ArchiveRedisList) redisTemplateForArchive.opsForValue().get(TOP_ARCHIVE);
            return result == null ? new ArchiveRedisList(new ArrayList<>()) : result;
        } catch (SerializationException e) {
            log.error("Redis 역직렬화 실패: {}", e.getMessage());
            redisTemplateForArchive.delete(TOP_ARCHIVE);
            return new ArchiveRedisList(new ArrayList<>());
        }
    }
}
