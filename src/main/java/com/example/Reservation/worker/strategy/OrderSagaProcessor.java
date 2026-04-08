package com.example.Reservation.worker.strategy;

import com.example.Reservation.dto.DeliveryStatusDto;

public interface OrderSagaProcessor {
    // 💡 1. 어떤 도메인에서 온 어떤 상태인지 검증 (예: "DELIVERY", "DELIVERED")
    boolean supports(String domain, String status);

    // 💡 2. 최신 상태로 조회해온 DTO 객체를 범용적(Object)으로 받아서 처리
    void process(Object latestDto);
}