package com.example.Reservation.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    public static final String PRODUCT_CACHE = "productCache";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PRODUCT_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500) // 최대 500개 상품까지 메모리에 캐싱
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .recordStats());
        return cacheManager;
    }
}