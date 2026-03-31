//package com.example.Reservation;
//
//import com.example.Reservation.event.OutboxEvent;
//import com.example.Reservation.repository.OutboxRepository;
//import com.example.Reservation.service.CouponService;
//import com.example.Reservation.service.QueueService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//public class CouponFlowIntegrationTest {
//
//    @Autowired private QueueService queueService;
//    @Autowired private CouponService couponService;
//    @Autowired private OutboxRepository outboxRepository;
//    @Autowired private StringRedisTemplate redisTemplate;
//
//    private static final String COUPON_COUNT_KEY = "event:coupon:count";
//    private static final String ACTIVE_TOKEN_KEY = "coupon_active:";
//
//    @BeforeEach
//    void setUp() {
//        // 1. 테스트 전 Redis 및 DB 완전 초기화
//        redisTemplate.getConnectionFactory().getConnection().flushAll();
//        outboxRepository.deleteAllInBatch();
//    }
//
//    @Test
//    @DisplayName("50명이 대기열에 진입하고, 스케줄러가 허용하면, 정확히 30명만 쿠폰을 발급받는다")
//    void testQueueAndCouponIssueFlow() throws InterruptedException {
//        // given
//        int totalUserCount = 50;   // 총 시도할 유저 수
//        int maxCouponCount = 30;   // 준비된 쿠폰 재고
//
//        // Redis에 쿠폰 재고 세팅
//        redisTemplate.opsForValue().set(COUPON_COUNT_KEY, String.valueOf(maxCouponCount));
//
//        ExecutorService executorService = Executors.newFixedThreadPool(32);
//        CountDownLatch latch = new CountDownLatch(totalUserCount);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//
//        // [백그라운드 스레드] 스케줄러 역할 시뮬레이션 (1초마다 10명씩 입장 허용)
//        Thread schedulerThread = new Thread(() -> {
//            try {
//                for (int i = 0; i < 5; i++) { // 10명씩 5번 = 50명 모두 허용할 때까지
//                    Thread.sleep(500); // 테스트를 위해 0.5초 주기로 단축
//                    queueService.allowUser(10); //
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        });
//        schedulerThread.start();
//
//        // when: 50명의 유저가 동시에 접속
//        for (int i = 0; i < totalUserCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    // 1. 대기열 진입 (Join Queue)
//                    String userId = queueService.registerQueue(); //
//
//                    // 2. 프론트엔드의 '폴링(Polling)' 시뮬레이션
//                    boolean isAllowed = false;
//                    while (!isAllowed) {
//                        QueueService.QueueStatusDto status = queueService.getStatus(userId); //
//                        if (status.entered()) {
//                            isAllowed = true;
//                        } else {
//                            Thread.sleep(100); // 0.1초 대기 후 다시 상태 확인
//                        }
//                    }
//
//                    // 3. 입장 허용됨! 쿠폰 발급 요청 (Controller 로직 시뮬레이션)
//                    if (Boolean.TRUE.equals(redisTemplate.hasKey(ACTIVE_TOKEN_KEY + userId))) { //
//                        couponService.requestCoupon(userId); //
//                        successCount.incrementAndGet();
//                    }
//
//                } catch (RuntimeException e) {
//                    // "선착순 마감되었습니다" 등의 예외 발생 시 카운트
//                    failCount.incrementAndGet();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        // 모든 스레드의 작업이 끝날 때까지 대기
//        latch.await();
//
//        // then: 결과 검증
//        List<OutboxEvent> outboxEvents = outboxRepository.findAll(); //
//
//        System.out.println("성공한 유저 수: " + successCount.get());
//        System.out.println("실패한 유저 수: " + failCount.get());
//        System.out.println("Outbox에 저장된 이벤트 수: " + outboxEvents.size());
//
//        // 1. Outbox 테이블에 정확히 30개의 이벤트만 저장되었는지 확인
//        assertThat(outboxEvents).hasSize(maxCouponCount);
//
//        // 2. 성공한 유저는 30명, 실패한(재고 소진으로 튕긴) 유저는 20명이어야 함
//        assertThat(successCount.get()).isEqualTo(maxCouponCount);
//        assertThat(failCount.get()).isEqualTo(totalUserCount - maxCouponCount);
//    }
//}