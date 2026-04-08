package com.example.Reservation.handler;

import com.example.Reservation.event.DeliveryUpdatedEvent;
import com.example.Reservation.event.OutboxEvent;
import com.example.Reservation.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventHandler {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    // 1️⃣ [데이터 복제용 - CQRS Read Model] 풀 페이로드 발행
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishForDataSync(DeliveryUpdatedEvent event) throws Exception {
        // 💡 "MongoDB"라는 단어를 완전히 제거하고 "데이터 동기화(Data Sync)"라는 범용적 목적만 명시합니다.
        log.info("📢 [Domain Event] 데이터 동기화(Sync) 프로세스 시작 - Delivery ID: {}, Event Type: {}",
                event.payload().id(), event.eventType());

        // 화면 조회나 검색 등 복제를 위한 모든 정보가 담긴 Full Payload 생성
        String fullPayload = objectMapper.writeValueAsString(event.payload());

        // 데이터 복제 전용 범용 토픽으로 발행
        saveOutbox(event.payload().id(), "delivery-sync-events", fullPayload, "SYNC");

        log.info("✅ [Outbox] 데이터 동기화용 풀 페이로드 저장 완료 - Topic: delivery-sync-events");
    }

    // 2️⃣ [비즈니스 연동용 - Saga] 진짜 제로 페이로드 발행 (주문, 결제, 알림 등)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void publishForSaga(DeliveryUpdatedEvent event) throws Exception {
        log.info("📢 [Domain Event] Saga 비즈니스 동기화 프로세스 시작 - Delivery ID: {}, Event Type: {}",
                event.payload().id(), event.eventType());

        // 💡 오직 원본 API를 찔러볼 수 있는 식별자(deliveryId)만 보내는 "진짜 제로 페이로드"
        Map<String, Object> zeroPayload = Map.of(
                "deliveryId", event.payload().id()
        );
        String json = objectMapper.writeValueAsString(zeroPayload);

        // 💡 수신자(Order)가 아닌 발행자(Delivery) 중심의 범용 비즈니스 토픽 사용
        saveOutbox(event.payload().id(), "delivery-domain-events", json, event.eventType());

        log.info("✅ [Outbox] 비즈니스 연동용 제로 페이로드 저장 완료 - Topic: delivery-domain-events, Type: {}", event.eventType());
    }

    // 공통 아웃박스 저장 메서드
    private void saveOutbox(Long id, String topic, String payload, String eventType) {
        OutboxEvent outbox = OutboxEvent.builder()
                .aggregateId(String.valueOf(id))
                .aggregateType("DELIVERY_" + eventType) // 예: DELIVERY_STATUS_CHANGED, DELIVERY_SYNC
                .topic(topic)
                .payload(payload)
                .status(OutboxEvent.EventStatus.READY)
                .createdAt(LocalDateTime.now()) // 💡 누락되었던 생성 시간 추가
                .build();

        outboxRepository.save(outbox);
    }
}