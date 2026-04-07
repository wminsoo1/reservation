package com.example.Reservation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber; // 운송장 번호
    private String receiverName;   // 수령인
    private String address;        // 배송지
    private String deliveryMemo;   // 배송 요청사항 (예: 문 앞에 두고 가주세요)

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // 현재 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private Rider rider; // 담당 기사

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 배송할 주문 내역

    private LocalDateTime updatedAt; // 버전 관리를 위한 타임스탬프

    // 배송 상세 정보 수정
    public void updateDeliveryInfo(String newAddress, String newMemo) {
        // 이미 집화(PICKUP) 이상 진행되었다면 주소 변경 차단
        if (this.status != DeliveryStatus.PENDING) {
            throw new IllegalStateException("이미 배송 처리가 시작되어 정보를 변경할 수 없습니다.");
        }
        this.address = newAddress;
        this.deliveryMemo = newMemo;
        this.updatedAt = LocalDateTime.now();
    }

    // 상태 전이 로직 (도메인 내부에서 엄격하게 관리)
    public void updateStatus(DeliveryStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("잘못된 상태 전이입니다: %s -> %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public enum DeliveryStatus {
        PENDING,   // 결제/접수 완료 (대기중)
        PICKUP,    // 기사님 집화 완료
        SHIPPED,   // 배송 출발
        DELIVERED;  // 배송 완료

        public boolean canTransitionTo(DeliveryStatus next) {
            return switch (this) {
                case PENDING -> next == PICKUP;
                case PICKUP -> next == SHIPPED;
                case SHIPPED -> next == DELIVERED;
                case DELIVERED -> false; // 완료 상태에선 변경 불가
            };
        }
    }
}