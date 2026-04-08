package com.example.Reservation.worker.sync;

import com.example.Reservation.document.DeliveryDriverView;
import com.example.Reservation.repository.DeliveryDriverMongoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverViewSyncStrategy implements MongoSyncStrategy {

    private final DeliveryDriverMongoRepository driverRepo;

    @Override
    public void sync(Long deliveryId, Long eventVersion, JsonNode payload) {
        // 1. 기존 도큐먼트 조회
        Optional<DeliveryDriverView> existingOpt = driverRepo.findByDeliveryId(deliveryId);

        // 2. 🛡️ 멱등성 검증
        if (existingOpt.isPresent() && existingOpt.get().getVersion() != null
                && eventVersion <= existingOpt.get().getVersion()) {
            log.info("⚠️ [Driver View] 과거 버전 무시 (Delivery ID: {}, Event Version: {})", deliveryId, eventVersion);
            return;
        }

        // 3. 💡 Null 안전성(Null-Safety) 검사 및 PK 생성
        // 배차가 안 된 상태(PENDING)일 경우 riderId가 null일 수 있으므로 방어 로직 추가
        boolean hasRider = payload.has("riderId") && !payload.get("riderId").isNull();
        String riderIdStr = hasRider ? payload.get("riderId").asText() : "UNASSIGNED";

        // 기사용 앱에서 빠른 조회를 위해 "RIDER_{riderId}_DEL_{deliveryId}" 조합으로 PK 사용
        String driverViewId = "RIDER_" + riderIdStr + "_DEL_" + deliveryId;

        // 메모 필드 역시 Null 방어
        String deliveryMemo = payload.has("deliveryMemo") && !payload.get("deliveryMemo").isNull()
                ? payload.get("deliveryMemo").asText() : "";

        // 4. 도큐먼트 조립
        DeliveryDriverView doc = DeliveryDriverView.builder()
                .id(existingOpt.isPresent() ? existingOpt.get().getId() : driverViewId) // 기존 ID가 있으면 덮어쓰기
                .riderId(hasRider ? payload.get("riderId").asLong() : null)
                .deliveryId(deliveryId)
                .version(eventVersion)
                .address(payload.get("address").asText())
                .deliveryMemo(deliveryMemo)
                .status(payload.get("status").asText())
                .build();

        // 5. 저장
        driverRepo.save(doc);
        log.info("✅ [Driver View] 동기화 완료 (Delivery ID: {})", deliveryId);
    }
}