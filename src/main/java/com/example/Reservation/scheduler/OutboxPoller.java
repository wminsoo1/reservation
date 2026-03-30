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

    // 1초마다 READY 상태인 이벤트를 찾아 카프카로 전송
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = outboxRepository.findAllByStatus(OutboxEvent.EventStatus.READY); //

        for (OutboxEvent event : events) {
            // 카프카로 메시지 전송 (Key는 유저 ID로 설정하여 순서 보장)
            kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload());

            // 상태를 PUBLISHED로 변경
            event.markAsPublished();
            log.info("카프카로 이벤트 발송 완료: {}", event.getAggregateId());
        }
    }
}