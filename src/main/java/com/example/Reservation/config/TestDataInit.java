package com.example.Reservation.config;

import com.example.Reservation.service.QueueService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private static final String ACTIVE_TOKEN_KEY = "coupon_active:";

    private final QueueService queueService;
    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_COUNT_KEY = "event:coupon:count";

    @Override
    public void run(String... args) throws Exception {
        log.info("========== [TEST] 가상 대기 인원 10명 등록 시작 ==========");
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 2. 쿠폰 재고 100개 설정
        redisTemplate.opsForValue().set(COUPON_COUNT_KEY, "10000");
        log.info("✔ 쿠폰 재고 설정: 100개");

        for (int i = 0; i < 5000; i++) {
            String userId = "test_user_" + i;
            // 입장권 발급 (TTL 1시간 넉넉하게)
            redisTemplate.opsForValue().set(ACTIVE_TOKEN_KEY + userId, "pass", Duration.ofMinutes(10));
        }
        log.info("✔ 테스트용 입장권 5,000장 발급 완료 (test_user_0 ~ 4999)");

        log.info("========== [TEST] 세팅 완료 ==========");


        for (int i = 1; i <= 10; i++) {
            String userId = queueService.registerQueue();
            log.info("가상 유저 {}명째 등록 완료: {}", i, userId);
        }

        log.info("========== [TEST] 가상 대기 인원 등록 완료 ==========");

    }
}