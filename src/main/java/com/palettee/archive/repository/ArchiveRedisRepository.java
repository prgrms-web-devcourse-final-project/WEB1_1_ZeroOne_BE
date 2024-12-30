package com.palettee.archive.repository;

import com.palettee.archive.domain.Archive;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
            String archiveId = incrKey.split(DELIMITER)[3];

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

    @Transactional(readOnly = true)
    public void updateArchiveList() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Archive> result = archiveRepository.findTopArchives(pageRequest);
        redisTemplateForArchive.opsForSet().remove(TOP_ARCHIVE);
        redisTemplateForArchive.opsForValue().set(TOP_ARCHIVE, result, 1, TimeUnit.MINUTES);
    }

    @SuppressWarnings("unchecked")
    public List<Archive> getTopArchives() {
        List<Archive> result = (List<Archive>) redisTemplateForArchive.opsForValue().get(TOP_ARCHIVE);
        return result == null ? new ArrayList<>() : result;
    }
}
