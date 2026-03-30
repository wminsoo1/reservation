package com.example.Reservation.service;

import com.example.Reservation.event.OutboxEvent;
import com.example.Reservation.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final StringRedisTemplate redisTemplate;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    // Lua Script (Bean으로 등록된 것을 주입받거나 직접 생성)
    private final RedisScript<Long> issueCouponScript;

    private static final String COUPON_COUNT_KEY = "event:coupon:count";
    private static final String COUPON_USERS_KEY = "event:coupon:users";

    /**
     * 쿠폰 발급 요청 (Outbox 패턴 적용 - 쿠폰 생성은 비동기 처리)
     */
    public void requestCoupon(String userId) {
        // 1. [Redis] Lua Script로 선착순 검증 & 차감 (Gatekeeper)
        // 리턴값: 1(성공), 0(재고없음), -1(이미발급됨)
        Long result = redisTemplate.execute(
                issueCouponScript,
                List.of(COUPON_USERS_KEY, COUPON_COUNT_KEY),
                userId
        );

        if (result == null || result == 0) {
            throw new RuntimeException("선착순 마감되었습니다.");
        }
        if (result == -1) {
            throw new RuntimeException("이미 발급된 계정입니다.");
        }

        // 2. [DB] Outbox 이벤트만 저장 (가장 가벼운 DB 작업)
        try {
            saveOutboxEventOnly(userId);
            log.info("쿠폰 요청 접수 완료 (Outbox 저장): {}", userId);

        } catch (Exception e) {
            log.error("Outbox 저장 실패. Redis 롤백 실행: {}", userId, e);
            revertRedisStock(userId);
            throw new RuntimeException("일시적인 오류로 쿠폰 요청에 실패했습니다.");
        }
    }

    // 트랜잭션 범위는 '이벤트 저장'에만 한정
    @Transactional
    public void saveOutboxEventOnly(String userId) throws Exception {
        // Payload 생성 (나중에 Worker가 이걸 보고 쿠폰을 만듦)
        Map<String, String> eventData = new HashMap<>();
        eventData.put("userId", userId);
        eventData.put("action", "ISSUE_COUPON");

        String payload = objectMapper.writeValueAsString(eventData);

        // 이벤트 생성 및 저장
        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(userId)
                .aggregateType("COUPON_REQUEST") // 타입 구분
                .topic("coupon-issue-request")   // Kafka 토픽
                .payload(payload)
                .status(OutboxEvent.EventStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(event);
    }

    // Redis 롤백 (보상 트랜잭션)
    private void revertRedisStock(String userId) {
        // 재고 +1 복구
        redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
        // 중복방지 Set에서 유저 삭제 (다시 시도할 수 있게)
        redisTemplate.opsForSet().remove(COUPON_USERS_KEY, userId);
    }

    @Transactional
    public void requestCouponForTest(String userId, long sleepTimeMillis) {
//        Long result = redisTemplate.execute(
//                issueCouponScript,
//                List.of(COUPON_USERS_KEY, COUPON_COUNT_KEY),
//                userId
//        );

//        if (result == null || result == 0) {
//            throw new RuntimeException("선착순 마감되었습니다.");
//        }

        try {
            Thread.sleep(sleepTimeMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("테스트 중 인터럽트 발생");
        }

        log.info("테스트 요청 완료 (지연 {}ms): {}", sleepTimeMillis, userId);
    }

}