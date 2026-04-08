package com.example.Reservation.worker.sync;

import com.example.Reservation.document.DeliveryCustomerView;
import com.example.Reservation.repository.DeliveryCustomerMongoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerViewSyncStrategy implements MongoSyncStrategy {

    private final DeliveryCustomerMongoRepository customerRepo;

    @Override
    public void sync(Long deliveryId, Long eventVersion, JsonNode payload) {
        // 1. 기존 도큐먼트 조회
        Optional<DeliveryCustomerView> existingOpt = customerRepo.findByDeliveryId(deliveryId);
        String trackingNumber = payload.get("trackingNumber").asText();

        // 2. 🛡️ 멱등성 검증 (기존 버전이 더 높거나 같으면 무시)
        if (existingOpt.isPresent() && existingOpt.get().getVersion() != null
                && eventVersion <= existingOpt.get().getVersion()) {
            log.info("⚠️ [Customer View] 과거 버전 무시 (Delivery ID: {}, Event Version: {})", deliveryId, eventVersion);
            return;
        }

        // 3. 업데이트할 새 데이터 빌드
        DeliveryCustomerView.DeliveryCustomerViewBuilder builder = DeliveryCustomerView.builder()
                .trackingNumber(trackingNumber)
                .deliveryId(deliveryId)
                .version(eventVersion)
                .receiverName(payload.get("receiverName").asText())
                .address(payload.get("address").asText())
                .status(payload.get("status").asText());

        // 4. 💡 기존에 조인되어 있던 데이터(기사이름, 주문 요약, 결제금액 등) 유지
        //    (배달 상태만 바뀌었을 때 다른 데이터가 증발하는 것을 방지)
        existingOpt.ifPresent(existing -> {
            builder.orderSummary(existing.getOrderSummary())
                    .totalPrice(existing.getTotalPrice())
                    .riderName(existing.getRiderName());
        });

        // 5. 저장
        customerRepo.save(builder.build());
        log.info("✅ [Customer View] 동기화 완료 (Delivery ID: {})", deliveryId);
    }
}