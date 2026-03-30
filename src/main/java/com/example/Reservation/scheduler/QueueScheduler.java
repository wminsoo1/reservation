package com.example.Reservation.scheduler;

import com.example.Reservation.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;

    private static final long ALLOW_COUNT = 10L;

    @Scheduled(fixedDelay = 1000)
    public void scheduleAllowUser() {
        queueService.allowUser(ALLOW_COUNT);
    }
}