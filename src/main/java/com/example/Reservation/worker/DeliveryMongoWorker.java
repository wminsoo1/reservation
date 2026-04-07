package com.example.Reservation.worker;

import com.example.Reservation.document.DeliveryDocument;
import com.example.Reservation.domain.Delivery;
import com.example.Reservation.repository.DeliveryRepository;
import com.example.Reservation.repository.DeliveryMongoRepository; // 추가됨
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryMongoWorker {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMongoRepository mongoRepository; // 정상 인식됨
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "delivery-sync-topic", groupId = "mongo-sync-group")
    public void syncToMongo(String message, Acknowledgment ack) {
        try {
            Long deliveryId = objectMapper.readTree(message).get("deliveryId").asLong();

            // 1. MySQL 조회 (Fetch Join 적용됨)
            Delivery delivery = deliveryRepository.findByIdWithRiderAndOrder(deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException("원본 없음"));

            // 2. [수정됨] 기존 MongoDB 문서의 ObjectID(String)를 먼저 찾아옵니다.
            String existingMongoId = mongoRepository.findByDeliveryId(deliveryId)
                    .map(DeliveryDocument::getId)
                    .orElse(null);

            // 3. Document 조립 시 기존 ID를 넣어줍니다. (Id가 있으면 Update, 없으면 Insert 로 동작)
            DeliveryDocument doc = DeliveryDocument.builder()
                    .id(existingMongoId) // 💡 여기가 에러 해결 포인트!
                    .deliveryId(delivery.getId())
                    .trackingNumber(delivery.getTrackingNumber())
                    .receiverName(delivery.getReceiverName())
                    .address(delivery.getAddress())
                    .deliveryMemo(delivery.getDeliveryMemo())
                    .status(delivery.getStatus().name())
                    // 연관 관계 데이터
                    .riderId(delivery.getRider() != null ? delivery.getRider().getId() : null)
                    .riderName(delivery.getRider() != null ? delivery.getRider().getName() : "배차 전")
                    .riderPhoneNumber(delivery.getRider() != null ? delivery.getRider().getPhoneNumber() : "")
                    // Order 연관 관계 (Order가 null일 경우를 대비한 안전망 추가)
                    .orderSummary(delivery.getOrder() != null ? delivery.getOrder().getOrderSummary() : "주문 정보 없음")
                    .totalPrice(delivery.getOrder() != null ? delivery.getOrder().getTotalPrice() : 0)
                    .build();

            // 4. 저장
            mongoRepository.save(doc);
            log.info("✅ MongoDB 동기화(Upsert) 완료: 택배ID={}", deliveryId);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("MongoDB 동기화 에러", e);
        }
    }
}