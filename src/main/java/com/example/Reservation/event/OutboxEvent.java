package com.example.Reservation.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;   // 예: 주문 ID, 유저 ID
    private String aggregateType; // 예: "RESERVATION"
    private String topic;         // Kafka 토픽 이름

    @Lob // JSON 데이터가 들어갈 예정이라 넉넉하게
    private String payload;       // 메시지 내용 (JSON)

    @Enumerated(EnumType.STRING)
    private EventStatus status;   // READY(대기), PUBLISHED(전송완료)

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt; // 언제 Kafka로 보냈는지

    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = EventStatus.COMPLETED;
    }

    public enum EventStatus {
        READY,      // 대기 중 (DB에만 저장됨)
        PUBLISHED,  // 카프카로 전송 완료
        COMPLETED   // 컨슈머가 최종적으로 쿠폰 발급까지 완료함
    }
}