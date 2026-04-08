package com.example.Reservation.document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "view_delivery_driver")
public class DeliveryDriverView {
    @Id
    private String id; // "RIDER_{riderId}_DEL_{deliveryId}" 조합으로 빠른 검색
    private Long riderId; // 인덱스를 걸어 기사별 목록 빠른 조회
    private Long deliveryId;
    private Long version;

    private String address;
    private String deliveryMemo; // 기사는 메모(문 앞 두고가세요 등)가 중요함
    private String status;
    // 💡 기사에게는 고객의 총 결제 금액(totalPrice) 정보가 굳이 필요 없으므로 제거 (Fit!)
}