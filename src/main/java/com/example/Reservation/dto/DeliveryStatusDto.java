package com.example.Reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class DeliveryStatusDto {
    private Long deliveryId;
    private Long orderId;
    private String status;
}