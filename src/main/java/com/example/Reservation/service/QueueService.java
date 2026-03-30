package com.example.Reservation.service;

import static java.lang.Boolean.*;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_QUEUE_KEY = "coupon_waiting_queue";
    private static final String ACTIVE_KEY_PREFIX = "coupon_active:"; // 입장권 Key

    public String registerQueue() {
        String userId = UUID.randomUUID().toString();

        long timeStamp = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, userId, timeStamp);

        return userId;
    }

    public void allowUser(long count) {
        Set<TypedTuple<String>> parsedUsers =
                redisTemplate.opsForZSet().popMin(WAITING_QUEUE_KEY, count);

        if (parsedUsers == null || parsedUsers.isEmpty()) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> tuple : parsedUsers) {
            String userId = tuple.getValue();


            redisTemplate.opsForValue()
                    .set(ACTIVE_KEY_PREFIX + userId, "ACCESS_TOKEN", Duration.ofMinutes(5));

            log.info("User Allowed: {}", userId);
        }
    }

    public QueueStatusDto getStatus(String userId) {
        // 1. 이미 입장 가능한 상태인지 확인
        if (TRUE.equals(redisTemplate.hasKey(ACTIVE_KEY_PREFIX + userId))) {
            return new QueueStatusDto(-1L, true);
        }

        // 2. 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, userId);
        if (rank == null) {
            return new QueueStatusDto(-1L, false);
        }

        return new QueueStatusDto(rank + 1, false);
    }

    public record QueueStatusDto(Long rank, boolean entered) {}
}