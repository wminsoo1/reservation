package com.example.Reservation.config;

import com.example.Reservation.domain.Product;
import com.example.Reservation.repository.ProductRepository;
import com.example.Reservation.service.QueueService;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInit implements CommandLineRunner {

    private static final String ACTIVE_TOKEN_KEY = "coupon_active:";

    private final QueueService queueService;
    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;

    private static final String COUPON_COUNT_KEY = "event:coupon:count";

    @Override
    public void run(String... args) throws Exception {
        log.info("========== [TEST] 가상 대기 인원 10명 등록 시작 ==========");
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 2. 쿠폰 재고 100개 설정
        redisTemplate.opsForValue().set(COUPON_COUNT_KEY, "10000");
        log.info("✔ 쿠폰 재고 설정: 100개");

        for (int i = 0; i < 5000; i++) {
            String userId = "test_user_" + i;
            // 입장권 발급 (TTL 1시간 넉넉하게)
            redisTemplate.opsForValue().set(ACTIVE_TOKEN_KEY + userId, "pass", Duration.ofMinutes(10));
        }
        log.info("✔ 테스트용 입장권 5,000장 발급 완료 (test_user_0 ~ 4999)");

        log.info("========== [TEST] 세팅 완료 ==========");


        for (int i = 1; i <= 10; i++) {
            String userId = queueService.registerQueue();
            log.info("가상 유저 {}명째 등록 완료: {}", i, userId);
        }

        log.info("========== [TEST] 가상 대기 인원 등록 완료 ==========");

        // --- [추가된 부분] 상품 마스터 더미데이터 삽입 ---
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    Product.builder()
                            .storeName("원조 장충동 할매 족발").rating(4.9).reviewCount("1,240+")
                            .menuName("족발(대) + 막국수 세트")
                            .imageUrl("https://images.unsplash.com/photo-1598514982205-f36b96d1e8d4?q=80&w=600&auto=format&fit=crop")
                            .originalPrice(45000).discountPrice(22500).discountRate(50)
                            .build(),
                    Product.builder()
                            .storeName("파리크라상 강남점").rating(4.8).reviewCount("850+")
                            .menuName("마감 빵 랜덤박스 (5~6구)")
                            .imageUrl("https://images.unsplash.com/photo-1509440159596-0249088772ff?q=80&w=600&auto=format&fit=crop")
                            .originalPrice(20000).discountPrice(6000).discountRate(70)
                            .build(),
                    Product.builder()
                            .storeName("바삭바삭 옛날통닭").rating(4.7).reviewCount("530+")
                            .menuName("후라이드 치킨 1마리")
                            .imageUrl("https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?q=80&w=600&auto=format&fit=crop")
                            .originalPrice(16000).discountPrice(9600).discountRate(40)
                            .build(),
                    Product.builder()
                            .storeName("스시야 (Sushi Ya)").rating(4.9).reviewCount("2,100+")
                            .menuName("오늘의 모둠초밥 12p")
                            .imageUrl("https://images.unsplash.com/photo-1579871494447-9811cf80d66c?q=80&w=600&auto=format&fit=crop")
                            .originalPrice(22000).discountPrice(15400).discountRate(30)
                            .build()
            ));
            log.info("✔ 배민 마감 특가 상품 더미데이터 4건 세팅 완료");
        }
    }
}