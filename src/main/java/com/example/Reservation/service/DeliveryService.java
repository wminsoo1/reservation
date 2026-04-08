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
    public void changeDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        // 1. 엔티티 조회
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 운송장입니다."));

        // 2. 도메인 메서드 호출 (내부에서 registerEvent 실행)
        delivery.updateStatus(newStatus);

        // 3. 리포지토리에 저장 (이때 스프링 데이터 JPA가 등록된 이벤트를 발행)
        // 별도로 outboxRepository.save()를 명시적으로 호출할 필요가 없습니다.
        deliveryRepository.save(delivery);
    }
}