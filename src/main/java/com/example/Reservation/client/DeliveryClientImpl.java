// client/DeliveryClientImpl.java
package com.example.Reservation.client;

import com.example.Reservation.domain.Delivery;
import com.example.Reservation.dto.DeliveryStatusDto;
import com.example.Reservation.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryClientImpl implements DeliveryClient {

    private final DeliveryRepository deliveryRepository;

    @Override
    public DeliveryStatusDto getDeliveryDto(Long deliveryId) {
        // 1. 원본 배달 엔티티 조회
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배달 정보를 찾을 수 없습니다."));

        // 2. 외부 모듈(Order)에게 엔티티를 숨기고 DTO만 반환!
        return new DeliveryStatusDto(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getStatus().name()
        );
    }
}