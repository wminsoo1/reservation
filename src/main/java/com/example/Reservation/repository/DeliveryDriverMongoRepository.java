package com.example.Reservation.repository;

import com.example.Reservation.document.DeliveryDriverView;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DeliveryDriverMongoRepository extends MongoRepository<DeliveryDriverView, String> {
    Optional<DeliveryDriverView> findByDeliveryId(Long deliveryId);
}