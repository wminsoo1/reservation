package com.example.Reservation.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderSummary; // 예: "반반족발(대) 외 1건"
    private int totalPrice;      // 예: 45000

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public void markAsDelivered() {
        // 1. 멱등성 방어: 이미 완료된 상태면 조용히 종료 (호출자에게 실패를 알릴 필요가 없을 때)
        if (this.status == OrderStatus.DELIVERY_COMPLETED) {
            return;
        }

        // 2. 비즈니스 규칙 방어: 에러 발생
        if (this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        // 3. 정상 상태 변경
        this.status = OrderStatus.DELIVERY_COMPLETED;
    }

    public enum OrderStatus {
        ORDER_RECEIVED, DELIVERING, DELIVERY_COMPLETED, CANCELED
    }
}