package com.example.Reservation.repository;

import com.example.Reservation.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByStatus(OutboxEvent.EventStatus status);
}