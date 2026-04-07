package com.example.Reservation.repository;

import com.example.Reservation.document.DeliveryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DeliveryMongoRepository extends MongoRepository<DeliveryDocument, String> {
    // MySQL의 PK인 deliveryId로 MongoDB 문서를 찾는 메서드
    Optional<DeliveryDocument> findByDeliveryId(Long deliveryId);
}