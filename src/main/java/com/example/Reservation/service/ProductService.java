package com.example.Reservation.service;

import com.example.Reservation.config.CacheConfig;
import com.example.Reservation.domain.Product;
import com.example.Reservation.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    @Cacheable(value = CacheConfig.PRODUCT_CACHE)
    public List<Product> getAllProducts() {
        log.info("⛔ [Cache Miss] DB에서 특가 상품 마스터 정보를 직접 조회합니다.");
        return productRepository.findAll();
    }

    @Transactional
    public void updateProductDiscount(Long productId, int newDiscountPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        product.updateDiscount(newDiscountPrice);

        log.info("📝 DB: {}번 상품의 할인 가격 수정 완료", productId);

        // 2. Redis Pub/Sub으로 캐시 무효화 이벤트 브로드캐스팅
        // -> 클러스터 내의 모든 서버 인스턴스가 이 메시지를 받고 자신의 로컬 캐시를 비웁니다.
        redisTemplate.convertAndSend("cache-evict-topic", "PRODUCT_CHANGED");
    }
}