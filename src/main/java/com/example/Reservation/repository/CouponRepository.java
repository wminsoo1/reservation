package com.example.Reservation.repository;

import com.example.Reservation.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByUserId(String userId);
    boolean existsByUserId(String userId);
}