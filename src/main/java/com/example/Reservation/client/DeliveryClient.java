package com.example.Reservation.client;

import com.example.Reservation.dto.DeliveryStatusDto;
import org.springframework.stereotype.Component;

// 실제 MSA에서는 @FeignClient가 될 부분입니다.
@Component
public interface DeliveryClient {
    DeliveryStatusDto getDeliveryDto(Long deliveryId);
}