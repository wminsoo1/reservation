package com.example.Reservation.event;

import com.example.Reservation.domain.Delivery;

// 💡 몽고DB 도큐먼트 업데이트에 필요한 핵심 정보만 담는 순수 DTO
public record DeliveryPayload(
        Long id,
        Long version,
        String trackingNumber,
        String receiverName,
        String address,
        String deliveryMemo,
        String status,
        Long riderId,
        Long orderId
) {
    // 엔티티를 받아서 DTO로 변환하는 편의 메서드
    public static DeliveryPayload from(Delivery delivery) {
        return new DeliveryPayload(
                delivery.getId(),
                delivery.getVersion(),
                delivery.getTrackingNumber(),
                delivery.getReceiverName(),
                delivery.getAddress(),
                delivery.getDeliveryMemo(),
                delivery.getStatus().name(),
                delivery.getRiderId(),
                delivery.getOrderId()
        );
    }
}