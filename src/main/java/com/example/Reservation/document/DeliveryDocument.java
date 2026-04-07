package com.example.Reservation.document;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "delivery_read_model")
public class DeliveryDocument {
    @Id
    private String id;
    private Long deliveryId;

    // 1. Delivery 정보
    private String trackingNumber;
    private String receiverName;
    private String address;
    private String deliveryMemo;
    private String status;

    // 2. 💡 Rider(기사) 조인 결과 플래트닝(Flattening)
    private Long riderId;
    private String riderName;
    private String riderPhoneNumber;

    // 3. 💡 Order(주문) 조인 결과 플래트닝
    private String orderSummary;
    private int totalPrice;
}