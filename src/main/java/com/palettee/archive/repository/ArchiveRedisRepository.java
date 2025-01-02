package com.palettee.archive.repository;

import com.palettee.archive.domain.Archive;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final String INCR_PATTERN = "incr:hit:archiveId:*";
    private static final String STD_PATTERN = "std:hit:archiveId:*";
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

            archiveRepository.updateHitCount(Long.parseLong(archiveId), totalHits);

            redisTemplate.opsForValue().set(stdKey, String.valueOf(totalHits));
            redisTemplate.delete(incrKey);
        }
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

        redisTemplateForArchive.opsForSet().remove(TOP_ARCHIVE);
        redisTemplateForArchive.opsForValue().set(TOP_ARCHIVE, result, 1, TimeUnit.HOURS);
    }

    private List<Long> getTop4IncrKeys() {
        Set<String> incrKeys = redisTemplate.keys(INCR_PATTERN);
        if (incrKeys == null || incrKeys.isEmpty()) {
            return Collections.emptyList();
        }

        return incrKeys.stream()
                .map(key -> new AbstractMap.SimpleEntry<>(key, getValueAsLong(key)))
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(4)
                .map(Map.Entry::getKey)
                .map(it -> getValueAsLong(extractId(it)))
                .collect(Collectors.toList());
    }

    private Long getValueAsLong(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    @SuppressWarnings("unchecked")
    public List<Archive> getTopArchives() {
        List<Archive> result = (List<Archive>) redisTemplateForArchive.opsForValue().get(TOP_ARCHIVE);
        return result == null ? new ArrayList<>() : result;
    }
}
