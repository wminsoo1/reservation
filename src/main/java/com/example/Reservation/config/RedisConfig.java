package com.example.Reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<Long> issueCouponScript() {
        Resource scriptSource = new ClassPathResource("scripts/issue_coupon.lua");
        return RedisScript.of(scriptSource, Long.class);
    }

}