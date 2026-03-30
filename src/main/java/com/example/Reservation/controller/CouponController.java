package com.example.Reservation.controller;

import com.example.Reservation.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final StringRedisTemplate redisTemplate; // Redis 직접 조회

    private static final String ACTIVE_TOKEN_KEY = "coupon_active:";

    @PostMapping("/issue")
    public ResponseEntity<?> issueCoupon(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");

        // 1. [검증] Redis에 '입장권(Active Token)'이 있는지 확인
        // hasKey: 키가 존재하면 true, 없으면 false (즉, 입장권 없으면 false)
        if (Boolean.FALSE.equals(redisTemplate.hasKey(ACTIVE_TOKEN_KEY + userId))) {
            return ResponseEntity.status(403).body("입장 불가능한 상태입니다. 대기열을 다시 확인해주세요.");
        }

        // 2. [발급] 쿠폰 발급 시도 (Redis Lua Script + Outbox)
        couponService.requestCoupon(userId);

        // 3. [응답] 성공 메시지
        Map<String, String> response = new HashMap<>();
        response.put("message", "쿠폰이 발급되었습니다!");
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-issue")
    public ResponseEntity<?> testIssueCoupon(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        int sleepTime = body.containsKey("sleepTime") ? (int) body.get("sleepTime") : 300;

//        // 1. [검증] 입장권 확인 (선택 사항: 테스트 목적에 따라 주석 처리 가능)
//        if (Boolean.FALSE.equals(redisTemplate.hasKey(ACTIVE_TOKEN_KEY + userId))) {
//            return ResponseEntity.status(403).body("입장 불가능한 상태입니다.");
//        }

        // 2. [테스트] DB 지연 시뮬레이션 서비스 호출
        couponService.requestCouponForTest(userId, sleepTime);

        return ResponseEntity.ok("테스트 요청 완료 (지연: " + sleepTime + "ms)");
    }

}