package com.example.Reservation.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.kafka.support.Acknowledgment;

@Getter
@AllArgsConstructor
public class CouponIssuedEvent {
    private final Acknowledgment ack;
    private final String userId;
}