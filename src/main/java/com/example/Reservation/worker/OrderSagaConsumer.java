package com.example.Reservation.worker;

import com.example.Reservation.client.DeliveryClient;
import com.example.Reservation.dto.DeliveryStatusDto;
import com.example.Reservation.worker.strategy.OrderSagaProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaConsumer {

    private final ObjectMapper objectMapper;
    private final DeliveryClient deliveryClient;

    // 결제, 배달, 쿠폰 등 모든 도메인의 프로세서가 주입됨
    private final List<OrderSagaProcessor> processors;

    @KafkaListener(topics = "delivery-domain-events", groupId = "order-saga-group")
    public void consumeDeliveryEvent(String message, Acknowledgment ack) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            Long deliveryId = payload.get("deliveryId").asLong();

            // 1. [API 검증] 원본 모듈을 찔러서 최신 상태의 DTO를 받아옴
            DeliveryStatusDto latestDto = deliveryClient.getDeliveryDto(deliveryId);

            // 2. [전략 실행] 해당 도메인("DELIVERY")과 상태에 맞는 프로세서 찾기
            for (OrderSagaProcessor processor : processors) {
                if (processor.supports("DELIVERY", latestDto.getStatus())) {
                    processor.process(latestDto); // 로직 실행!
                    break;
                }
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ 배달 Saga 이벤트 처리 중 오류 발생", e);
        }
    }
}