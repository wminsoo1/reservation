package com.example.Reservation.service;

import com.example.Reservation.domain.Delivery;
import com.example.Reservation.domain.Delivery.DeliveryStatus;
import com.example.Reservation.event.OutboxEvent;
import com.example.Reservation.repository.DeliveryRepository;
import com.example.Reservation.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void changeDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) throws Exception {
        // 1. MySQL 원본 상태 업데이트
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운송장입니다."));

        delivery.updateStatus(newStatus);
        log.info("📦 DB: {}번 택배 상태 변경 완료 -> {}", deliveryId, newStatus);

        // 2. 제로 페이로드 Outbox 이벤트 발행 (상태값 없이 ID만 전송)
        Map<String, Long> payloadMap = new HashMap<>();
        payloadMap.put("deliveryId", deliveryId);
        String zeroPayload = objectMapper.writeValueAsString(payloadMap);

        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(String.valueOf(deliveryId))
                .aggregateType("DELIVERY_STATUS_CHANGE")
                .topic("delivery-notification-topic") // 알림톡 발송용 토픽
                .payload(zeroPayload)
                .status(OutboxEvent.EventStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(event);
    }
}