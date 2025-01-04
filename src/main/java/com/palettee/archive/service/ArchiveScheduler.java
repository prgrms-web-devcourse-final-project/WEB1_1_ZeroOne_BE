package com.palettee.archive.service;

import com.palettee.archive.repository.ArchiveRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArchiveScheduler {

    private final ArchiveRedisRepository archiveRedisRepository;

    @Scheduled(cron = "0 * * * * *")
    public void updateMainArchive() {
        archiveRedisRepository.updateArchiveList();
        archiveRedisRepository.settleHits();
    }

}
