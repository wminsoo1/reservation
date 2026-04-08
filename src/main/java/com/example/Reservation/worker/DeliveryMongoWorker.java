package com.example.Reservation.worker;

import com.example.Reservation.document.DeliveryDocument;
import com.example.Reservation.domain.Delivery;
import com.example.Reservation.repository.DeliveryRepository;
import com.example.Reservation.repository.DeliveryMongoRepository; // 추가됨
import com.example.Reservation.worker.sync.MongoSyncStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryMongoWorker {

    private final ObjectMapper objectMapper;
    private final List<MongoSyncStrategy> strategies;

    @KafkaListener(topics = "delivery-sync-events", groupId = "mongo-sync-group")
    public void syncToMongo(String message, Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            Long deliveryId = payload.get("id").asLong();
            Long eventVersion = payload.has("version") ? payload.get("version").asLong() : 0L;

            log.info("🔄 [Mongo Sync] 데이터 동기화 시작 (Delivery ID: {})", deliveryId);

            // 💡 뷰가 100개로 늘어나도 알아서 100번 돌면서 각각 업데이트를 수행합니다.
            strategies.forEach(strategy -> strategy.sync(deliveryId, eventVersion, payload));

            ack.acknowledge();
        } catch (Exception e) {
            log.error("MongoDB 동기화 에러", e);
        }
    }

}