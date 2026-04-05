package com.example.Reservation.listener;

import com.example.Reservation.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {

    private final CacheManager cacheManager;

    // Redis Pub/Sub 메시지가 도착하면 자동으로 실행되는 메서드
    public void onMessage(String message) {
        log.info("📢 [Pub/Sub 수신] 캐시 무효화 메시지 도착: {}", message);

        if ("PRODUCT_CHANGED".equals(message)) {
            // Caffeine 로컬 캐시에 접근하여 통째로 비웁니다.
            Cache cache = cacheManager.getCache(CacheConfig.PRODUCT_CACHE);
            if (cache != null) {
                cache.clear();
                log.info("🗑️ 서버 로컬 메모리의 상품 캐시를 성공적으로 초기화했습니다.");
            }
        }
    }
}