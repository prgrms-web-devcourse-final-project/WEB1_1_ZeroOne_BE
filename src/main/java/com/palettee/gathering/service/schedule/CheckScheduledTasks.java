package com.palettee.gathering.service.schedule;

import com.palettee.gathering.service.GatheringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckScheduledTasks {

    private final GatheringService gatheringService;

    // 오전 12시와 오후 12시에 스케줄 실행
    @Scheduled(cron = "0 0 0,12 * * ?")
    public void checkScheduledTasks() {
        log.info("만료 스케줄러 실행");
        gatheringService.updateGatheringStatus();
    }






}
