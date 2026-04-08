package com.example.Reservation.worker.strategy;

import com.example.Reservation.domain.Order;
import com.example.Reservation.dto.DeliveryStatusDto;
import com.example.Reservation.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCompletedProcessor implements OrderSagaProcessor {

    private final OrderRepository orderRepository;

    @Override
    public boolean supports(String domain, String status) {
        // 배달(DELIVERY) 도메인에서 온 배달완료(DELIVERED) 이벤트일 때만 동작!
        return "DELIVERY".equals(domain) && "DELIVERED".equals(status);
    }

    @Override
    @Transactional
    public void process(Object latestDto) {
        // 넘어온 Object를 Delivery DTO로 캐스팅하여 사용
        DeliveryStatusDto dto = (DeliveryStatusDto) latestDto;

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        try {
            // 1. 도메인 메서드 호출
            // - 이미 완료된 상태면 내부에서 return 되므로 자연스럽게 로그 찍고 종료됨 (멱등성)
            // - 취소된 상태면 IllegalStateException 발생
            order.markAsDelivered();

            log.info("🎉 [Saga] 주문 완료 처리 성공 (주문 ID: {})", order.getId());

        } catch (IllegalStateException e) {
            // 2. 비즈니스 규칙 위반 (예: 이미 취소된 주문)
            log.error("🚨 [Saga 비즈니스 예외] 상태 변경 불가: {} (주문 ID: {})", e.getMessage(), order.getId());

            /*
             * [보상 트랜잭션 또는 후속 조치 정의]
             * * CASE 1: 재시도 큐(DLT)로 전송
             * - 바로 에러를 던지지 않고 별도의 에러 큐(Dead Letter Topic)에 메시지를 발행하여 관리자가 수동 처리하게 함.
             * - producer.sendToRetryQueue(dto);
             * * CASE 2: 보상 트랜잭션 실행
             * - 이미 결제된 금액이 있다면 결제 취소 이벤트를 발행하거나 외부 API 호출.
             * - paymentService.cancel(order.getPaymentId());
             * * CASE 3: 알림 서비스 연동
             * - 슬랙(Slack)이나 이메일로 관리자에게 즉시 알림 발송.
             * - alertService.sendCriticalAlert(order.getId(), e.getMessage());
             */

            // 카프카 무한 재시도를 막기 위해 예외를 밖으로 던지지 않고 여기서 마무리하거나,
            // 특정 커스텀 예외를 던져 ErrorHandler에서 처리하게 합니다.
        } catch (Exception e) {
            // 3. 시스템 예외 (DB 장애, 네트워크 장애 등)
            log.error("🔥 [Saga 시스템 예외] 예상치 못한 오류 발생 (주문 ID: {})", order.getId());

            // 시스템 장애는 재시도가 의미가 있으므로, 예외를 다시 던져서 Kafka의 Retry 메커니즘을 타게 함
            throw e;
        }
    }
}