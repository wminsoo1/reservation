package com.example.Reservation.worker.sync;

import com.fasterxml.jackson.databind.JsonNode;

public interface MongoSyncStrategy {
    // 풀 페이로드를 받아서 각자 맡은 View 모델을 업데이트합니다.
    void sync(Long deliveryId, Long eventVersion, JsonNode payload);
}