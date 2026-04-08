package com.example.Reservation.scheduler;

import com.example.Reservation.event.OutboxEvent;
import com.example.Reservation.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 1초마다 READY 상태인 아웃박스 이벤트를 조회하여 카프카로 발송
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = outboxRepository.findAllByStatus(OutboxEvent.EventStatus.READY);

        if (!events.isEmpty()) {
            log.info("🚀 대기 중인 Outbox 이벤트 {}건 카프카 전송 시작", events.size());
        }

        for (OutboxEvent event : events) {
            try {
                // 카프카 전송 (토픽은 이벤트에 저장된 값 사용)
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload());

                // 성공 시 상태 변경
                event.markAsPublished();
                log.info("✅ 카프카 전송 완료 - Topic: {}, ID: {}", event.getTopic(), event.getAggregateId());
            } catch (Exception e) {
                log.error("❌ 카프카 전송 실패 - ID: {}", event.getId(), e);
                // 실패한 건은 READY 상태로 남아 다음 폴링 때 재시도됨 (최소 1회 보장)
            }
        }
    }
}