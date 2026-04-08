package com.example.Reservation.repository;

import com.example.Reservation.document.DeliveryCustomerView;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DeliveryCustomerMongoRepository extends MongoRepository<DeliveryCustomerView, String> {
    // MySQL 원본의 PK인 deliveryId로 문서를 찾는 메서드
    Optional<DeliveryCustomerView> findByDeliveryId(Long deliveryId);
}