package com.example.Reservation.repository;

import com.example.Reservation.domain.Delivery;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d " +
            "LEFT JOIN FETCH d.rider " +
            "LEFT JOIN FETCH d.order " +
            "WHERE d.id = :id")
    Optional<Delivery> findByIdWithRiderAndOrder(@Param("id") Long id);
}
