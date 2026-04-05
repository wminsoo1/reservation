package com.example.Reservation.worker;

import com.example.Reservation.domain.Coupon;
import com.example.Reservation.event.OutboxEvent;
import com.example.Reservation.repository.CouponRepository;
import com.example.Reservation.repository.OutboxRepository;
import com.example.Reservation.service.CouponService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponKafkaConsumer {

    private final CouponRepository couponRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final CouponService couponService;

    @KafkaListener(topics = "coupon-issue-request", groupId = "coupon-worker-group")
    @Transactional
    public void consumeCouponRequest(String message, Acknowledgment ack) {
        try {
            // 1. 카프카 메시지(JSON) 파싱
            JsonNode payload = objectMapper.readTree(message);
            String userId = payload.get("userId").asText();

            log.info("✅ [Kafka Consumer] 쿠폰 발급 작업 수신: {}", userId);

            // 2. [핵심] 의도적인 지연 (5초) - 실무의 복잡한 비즈니스 로직 처리 시간 가정
            log.info("⏳ [Kafka Consumer] 쿠폰 생성 중... (5초 소요)");
            Thread.sleep(5000);

            // 3. 실제 쿠폰 엔티티 저장
            couponService.processCouponIssue(userId, ack);

            log.info("🎉 [Kafka Consumer] 쿠폰 발급 최종 완료: {}", userId);

        } catch (Exception e) {
            log.error("카프카 메시지 처리 중 치명적 오류 발생", e);
        }
    }
}