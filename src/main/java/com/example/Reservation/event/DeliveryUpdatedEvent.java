package com.example.Reservation.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryUpdatedEvent(
        String eventId,        // 💡 [운영] 이벤트 고유 식별자 (추적, 로그, 멱등성 검증용)
        DeliveryPayload payload,     // [비즈니스] 변경된 배달의 ID (Zero Payload의 핵심)
        String eventType,      // 💡 [운영] 어떤 종류의 변경인지 (예: "STATUS_CHANGED", "INFO_UPDATED")
        LocalDateTime occurredAt // 💡 [운영] 이벤트가 실제로 발생한 시간
) {
    // 엔티티에서 이벤트를 쉽게 생성하기 위한 편의 메서드
    public static DeliveryUpdatedEvent of(Long deliveryId, String eventType) {
        return new DeliveryUpdatedEvent(
                UUID.randomUUID().toString(), // 식별자 자동 생성
                deliveryId,
                eventType,
                LocalDateTime.now()           // 발생 시간 자동 기록
        );
    }
}