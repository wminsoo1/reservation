package com.example.Reservation.document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "view_delivery_customer")
public class DeliveryCustomerView {
    @Id
    private String trackingNumber; // 고객은 운송장 번호로 조회하므로 PK
    private Long deliveryId;
    private Long version;

    private String receiverName;
    private String address;
    private String status;
    private String orderSummary;
    private int totalPrice; // 고객은 내 결제 금액이 궁금함
    private String riderName;
}